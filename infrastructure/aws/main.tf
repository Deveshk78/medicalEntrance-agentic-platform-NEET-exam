terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  default = "us-east-1"
}

variable "project_name" {
  default = "medent-agent-platform"
}

variable "domain_name" {
  description = "Domain for the application"
  type        = string
}

# ─── AWS Cognito User Pool ───────────────────────────────────────────
resource "aws_cognito_user_pool" "medent" {
  name = "${var.project_name}-users"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 12
    require_lowercase = true
    require_numbers   = true
    require_symbols   = true
    require_uppercase = true
  }

  schema {
    name                = "roll_number"
    attribute_data_type = "String"
    mutable             = true
    required            = false
    string_attribute_constraints {
      min_length = 1
      max_length = 20
    }
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }
}

resource "aws_cognito_user_pool_group" "admin" {
  name         = "ADMIN"
  user_pool_id = aws_cognito_user_pool.medent.id
  description  = "Administrators with full access"
}

resource "aws_cognito_user_pool_group" "student" {
  name         = "STUDENT"
  user_pool_id = aws_cognito_user_pool.medent.id
  description  = "Students taking medical entrance exams"
}

resource "aws_cognito_user_pool_group" "observability" {
  name         = "OBSERVABILITY"
  user_pool_id = aws_cognito_user_pool.medent.id
  description  = "Observability and SRE team"
}

resource "aws_cognito_user_pool_client" "web_client" {
  name         = "${var.project_name}-web"
  user_pool_id = aws_cognito_user_pool.medent.id

  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code", "implicit"]
  allowed_oauth_scopes                 = ["email", "openid", "profile"]
  supported_identity_providers         = ["COGNITO"]
  callback_urls                        = ["https://${var.domain_name}/callback"]
  logout_urls                          = ["https://${var.domain_name}/logout"]

  explicit_auth_flows = [
    "ALLOW_USER_SRP_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
  ]

  access_token_validity  = 1
  id_token_validity      = 1
  refresh_token_validity = 30

  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }
}

resource "aws_cognito_user_pool_domain" "medent" {
  domain       = "${var.project_name}-auth"
  user_pool_id = aws_cognito_user_pool.medent.id
}

# ─── AWS WAF v2 ──────────────────────────────────────────────────────
resource "aws_wafv2_web_acl" "medent" {
  name        = "${var.project_name}-waf"
  description = "WAF for MedEnt Agent Platform - 25L student capacity"
  scope       = "CLOUDFRONT"

  default_action {
    allow {}
  }

  rule {
    name     = "RateLimitRule"
    priority = 1
    action { block {} }
    statement {
      rate_based_statement {
        limit              = 2000
        aggregate_key_type = "IP"
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "RateLimitRule"
      sampled_requests_enabled   = true
    }
  }

  rule {
    name     = "AWSManagedRulesCommonRuleSet"
    priority = 2
    override_action { none {} }
    statement {
      managed_rule_group_statement {
        name        = "AWSManagedRulesCommonRuleSet"
        vendor_name = "AWS"
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "CommonRuleSet"
      sampled_requests_enabled   = true
    }
  }

  rule {
    name     = "AWSManagedRulesKnownBadInputsRuleSet"
    priority = 3
    override_action { none {} }
    statement {
      managed_rule_group_statement {
        name        = "AWSManagedRulesKnownBadInputsRuleSet"
        vendor_name = "AWS"
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "KnownBadInputs"
      sampled_requests_enabled   = true
    }
  }

  rule {
    name     = "AWSManagedRulesSQLiRuleSet"
    priority = 4
    override_action { none {} }
    statement {
      managed_rule_group_statement {
        name        = "AWSManagedRulesSQLiRuleSet"
        vendor_name = "AWS"
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "SQLiRuleSet"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.project_name}-waf"
    sampled_requests_enabled   = true
  }
}

# ─── AWS Shield Advanced ─────────────────────────────────────────────
resource "aws_shield_protection" "medent" {
  name         = "${var.project_name}-shield"
  resource_arn = aws_cloudfront_distribution.medent.arn
}

resource "aws_cloudfront_distribution" "medent" {
  enabled             = true
  default_root_object = "index.html"
  web_acl_id          = aws_wafv2_web_acl.medent.arn

  origin {
    domain_name = var.domain_name
    origin_id   = "medent-alb"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "medent-alb"
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = true
      headers      = ["Authorization", "Host"]
      cookies { forward = "all" }
    }
  }

  restrictions {
    geo_restriction { restriction_type = "none" }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }
}

output "cognito_user_pool_id" {
  value = aws_cognito_user_pool.medent.id
}

output "cognito_client_id" {
  value = aws_cognito_user_pool_client.web_client.id
}

output "cognito_jwks_url" {
  value = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.medent.id}/.well-known/jwks.json"
}

output "waf_acl_arn" {
  value = aws_wafv2_web_acl.medent.arn
}

output "cloudfront_domain" {
  value = aws_cloudfront_distribution.medent.domain_name
}

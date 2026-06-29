from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    mongo_uri: str = "mongodb://localhost:27017/medent"
    redis_host: str = "localhost"
    redis_port: int = 6379
    rabbitmq_host: str = "localhost"
    rabbitmq_port: int = 5672
    rabbitmq_user: str = "medent"
    rabbitmq_pass: str = "medent_secret"

    class Config:
        env_file = ".env"


settings = Settings()

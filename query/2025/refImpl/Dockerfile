FROM python:3.12-slim-bookworm
COPY --from=ghcr.io/astral-sh/uv:latest /uv /uvx /bin/

COPY ./ /app
WORKDIR /app/

RUN uv sync --frozen

CMD ["uv", "run", "solution.py"]
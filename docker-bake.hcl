variable "TAG" {
  default = "latest"
}

group "default" {
  targets = ["app"]
}

target "app" {
  dockerfile = "Dockerfile"
  tags = ["flytegg/ls-discord-bot:${TAG}"]
  cache-from = ["type=registry,ref=flytegg/ls-discord-bot:cache"]
  cache-to = ["type=registry,ref=flytegg/ls-discord-bot:cache,mode=max"]
}

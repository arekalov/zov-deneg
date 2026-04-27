FROM golang:trixie

WORKDIR /zov-go

COPY . .

RUN go mod tidy
RUN go build -o collector ./cmd/collector

CMD ["./collector"]
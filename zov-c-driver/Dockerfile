FROM gcc:latest

WORKDIR /c-driver

COPY driver.c .

RUN gcc -O2 driver.c -o driver

CMD ["./driver"]
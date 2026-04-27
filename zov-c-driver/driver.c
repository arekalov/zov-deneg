#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <time.h>
#include <errno.h>

#define SOCKET_PATH "/tmp/zovdengi/driver.sock"

#define MAGIC 0xDACE
#define VERSION 0x01

#define MSG_HEARTBEAT     0x00
#define MSG_QUOTE         0x01
#define MSG_ORDERBOOK     0x02
#define MSG_SESSION_START 0x10
#define MSG_SESSION_END   0x11

#define PRICE_SCALE 100000000LL
#define SEC_COUNT 5

#pragma pack(push, 1)
typedef struct {
    uint16_t magic;
    uint8_t  version;
    uint8_t  msg_type;
    uint32_t payload_len;
    uint8_t  checksum;
} Header;
#pragma pack(pop)

// ─────────────────────────────────────────────
// Глобальные данные
// ─────────────────────────────────────────────
// TODO: replace with real uuid from db 
uint8_t securities[SEC_COUNT][16] = {
    // существующие
    {'S','B','E','R',0,0,0,0,0,0,0,0,0,0,0,0},
    {'G','A','Z','P',0,0,0,0,0,0,0,0,0,0,0,0},
    {'L','K','O','H',0,0,0,0,0,0,0,0,0,0,0,0},
    {'T','C','S','G',0,0,0,0,0,0,0,0,0,0,0,0},
    {'Y','N','D','X',0,0,0,0,0,0,0,0,0,0,0,0},
    {'V','T','B','R',0,0,0,0,0,0,0,0,0,0,0,0},
    {'R','O','S','N',0,0,0,0,0,0,0,0,0,0,0,0},
    {'S','I','L','V',0,0,0,0,0,0,0,0,0,0,0,0},
    {'M','T','S','S',0,0,0,0,0,0,0,0,0,0,0,0},
    {'G','M','K','N',0,0,0,0,0,0,0,0,0,0,0,0},

    // + 8 новых
    {'A','P','P','L',0,0,0,0,0,0,0,0,0,0,0,0},
    {'T','S','L','A',0,0,0,0,0,0,0,0,0,0,0,0},
    {'A','M','Z','N',0,0,0,0,0,0,0,0,0,0,0,0},
    {'M','S','F','T',0,0,0,0,0,0,0,0,0,0,0,0},
    {'N','V','D','A',0,0,0,0,0,0,0,0,0,0,0,0},
    {'B','T','C','X',0,0,0,0,0,0,0,0,0,0,0,0},
    {'E','T','H','X',0,0,0,0,0,0,0,0,0,0,0,0},
    {'G','O','O','G',0,0,0,0,0,0,0,0,0,0,0,0}
};

double prices[SEC_COUNT] = {100.0, 200.0, 300.0, 400.0, 500.0};

typedef struct {
    double mid;
    uint32_t ask_qty[ORDERBOOK_LEVELS];
    uint32_t bid_qty[ORDERBOOK_LEVELS];
} OBState;

OBState ob_state[SEC_COUNT];
uint64_t snapshot_id = 1;

// ─────────────────────────────────────────────
// Utils
// ─────────────────────────────────────────────

int64_t now_ms() {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return ts.tv_sec * 1000LL + ts.tv_nsec / 1000000;
}

uint8_t calc_checksum(uint8_t *data, uint32_t len) {
    uint8_t xor = 0;
    for (uint32_t i = 0; i < len; i++) {
        xor ^= data[i];
    }
    return xor;
}

// ─────────────────────────────────────────────
// Socket
// ─────────────────────────────────────────────

int connect_socket() {
    int sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("socket");
        exit(1);
    }

    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, SOCKET_PATH, sizeof(addr.sun_path)-1);

    if (connect(sock, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("connect");
        exit(1);
    }

    printf("[driver] connected to %s\n", SOCKET_PATH);
    return sock;
}

// ─────────────────────────────────────────────
// Send message
// ─────────────────────────────────────────────

void send_message(int sock, uint8_t type, uint8_t *payload, uint32_t len) {
    Header h;
    h.magic = MAGIC;
    h.version = VERSION;
    h.msg_type = type;
    h.payload_len = len;
    h.checksum = calc_checksum(payload, len);

    if (write(sock, &h, sizeof(h)) < 0) {
        perror("write header");
        exit(1);
    }

    if (len > 0) {
        if (write(sock, payload, len) < 0) {
            perror("write payload");
            exit(1);
        }
    }
}

// ─────────────────────────────────────────────
// SessionStart
// ─────────────────────────────────────────────

void send_session_start(int sock) {
    uint32_t payload_size = 10 + SEC_COUNT * 16;
    uint8_t *payload = malloc(payload_size);

    int64_t ts = now_ms();
    memcpy(payload, &ts, 8);

    uint16_t count = SEC_COUNT;
    memcpy(payload + 8, &count, 2);

    for (int i = 0; i < SEC_COUNT; i++) {
        memcpy(payload + 10 + i * 16, securities[i], 16);
    }

    send_message(sock, MSG_SESSION_START, payload, payload_size);
    free(payload);

    printf("[driver] session start: %d securities\n", SEC_COUNT);
}

// ─────────────────────────────────────────────
// Quote
// ─────────────────────────────────────────────

void send_quote(int sock, int idx) {
    uint8_t payload[36];

    int64_t ts = now_ms();

    // движение цены
    double delta = ((rand() % 200) - 100) / 100.0;
    prices[idx] += delta;

    int64_t price_fp = (int64_t)(prices[idx] * PRICE_SCALE);
    uint32_t volume = rand() % 1000 + 1;

    memcpy(payload, securities[idx], 16);
    memcpy(payload + 16, &ts, 8);
    memcpy(payload + 24, &price_fp, 8);
    memcpy(payload + 32, &volume, 4);

    send_message(sock, MSG_QUOTE, payload, sizeof(payload));
}

// ───────────────────────────────
// OrderBook
// ───────────────────────────────

void init_ob() {
    for (int i = 0; i < SEC_COUNT; i++) {
        ob_state[i].mid = prices[i];
        for (int j = 0; j < ORDERBOOK_LEVELS; j++) {
            ob_state[i].ask_qty[j] = 50 + rand() % 100;
            ob_state[i].bid_qty[j] = 50 + rand() % 100;
        }
    }
}

void send_orderbook(int sock, int i) {
    uint8_t buf[36 + ORDERBOOK_LEVELS * 2 * 12];

    int64_t ts = now_ms();
    double mid = prices[i];

    mid += ((rand() % 200) - 100) / 100.0;
    ob_state[i].mid = mid;

    double spread = 0.5;

    int off = 0;

    memcpy(buf + off, securities[i], 16);
    off += 16;

    memcpy(buf + off, &ts, 8);
    off += 8;

    memcpy(buf + off, &snapshot_id, 8);
    off += 8;
    snapshot_id++;

    uint16_t a = ORDERBOOK_LEVELS;
    uint16_t b = ORDERBOOK_LEVELS;

    memcpy(buf + off, &a, 2);
    off += 2;
    memcpy(buf + off, &b, 2);
    off += 2;

    for (int k = 0; k < ORDERBOOK_LEVELS; k++) {
        int64_t ap = (mid + spread * (k + 1)) * PRICE_SCALE;
        int64_t bp = (mid - spread * (k + 1)) * PRICE_SCALE;

        ob_state[i].ask_qty[k] = 20 + rand() % 200;
        ob_state[i].bid_qty[k] = 20 + rand() % 200;

        memcpy(buf + off, &ap, 8); off += 8;
        memcpy(buf + off, &ob_state[i].ask_qty[k], 4); off += 4;
    }

    for (int k = 0; k < ORDERBOOK_LEVELS; k++) {
        int64_t bp = (mid - spread * (k + 1)) * PRICE_SCALE;

        memcpy(buf + off, &bp, 8); off += 8;
        memcpy(buf + off, &ob_state[i].bid_qty[k], 4); off += 4;
    }

    send_msg(sock, MSG_ORDERBOOK, buf, off);
}

void send_session_end(int sock) {
    uint8_t payload[8];

    int64_t ts = now_ms();
    memcpy(payload, &ts, 8);

    send_message(sock, MSG_SESSION_END, payload, 8);

    printf("[driver] session end\n");
}

int global_socket;

void handle_sigint(int sig) {
    send_session_end(global_socket);
    close(global_socket);
    exit(0);
}

// ─────────────────────────────────────────────
// Main
// ─────────────────────────────────────────────

int main() {
    srand(time(NULL));

    global_socket = connect_socket();

    send_session_start(sock);
    signal(SIGINT, handle_sigint);

    int counter = 0;

    while (1) {
        // случайный инструмент
        int idx = rand() % SEC_COUNT;

        send_quote(sock, idx);
        send_orderbook(sock, idx);

        // heartbeat раз в ~5 сек
        if (++counter % 50 == 0) {
            send_message(sock, MSG_HEARTBEAT, NULL, 0);
            printf("[driver] heartbeat\n");
        }

        usleep(100000); // 100 ms
    }

    return 0;
}
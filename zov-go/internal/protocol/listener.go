package protocol

import (
    "fmt"
    "net"
    "os"
    "path/filepath"
)

func Listen(socketPath string) (net.Listener, error) {
    dir := filepath.Dir(socketPath)
    if err := os.MkdirAll(dir, 0755); err != nil {
        return nil, fmt.Errorf("mkdir %s: %w", dir, err)
    }

    os.Remove(socketPath)

    ln, err := net.Listen("unix", socketPath)
    if err != nil {
        return nil, fmt.Errorf("listen %s: %w", socketPath, err)
    }

    return ln, nil
}
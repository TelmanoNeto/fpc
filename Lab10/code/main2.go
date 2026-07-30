package main

import (
	"fmt"
	"time"
)

func mensagem() {
	fmt.Println("Executando mensagem")

}

func main() {
	go mensagem()
	time.Sleep(time.Second)
	fmt.Println("Fim da main")
}

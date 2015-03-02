package gamescheduler

import (
	"testing"
	"time"
)

func TestGameProcessStop(t *testing.T) {
	gp := gameProcess{
		name: "test",
		exec: []string{"sleep", "1"},

		done: make(chan error),
	}

	timer := time.NewTimer(time.Millisecond * 200)

	go func() {
		gp.start()
		timer.Reset(0)
	}()

	select {
	case <-time.After(time.Millisecond * 100):
		gp.stop()
	case <-timer.C:
		t.FailNow()
	}
}

func TestGameProcessAlreadyStopped(t *testing.T) {
	gp := gameProcess{
		name: "test",
		exec: []string{"sleep", "0.1"},

		done: make(chan error),
	}

	timer := time.NewTimer(time.Second * 1)

	go func() {
		gp.start()
		timer.Reset(0)
	}()

	<-timer.C
	gp.stop()
}

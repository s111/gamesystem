package gamescheduler

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestGameScheduler(t *testing.T) {
	go Run()

	assert.Equal(t, "", GetCurrent())

	gp1 := gameProcess{
		name: "game1",
		exec: []string{"sleep", "0.1"},

		done: make(chan error),
	}

	gp2 := gameProcess{
		name: "game2",
		exec: []string{"sleep", "0.1"},

		done: make(chan error),
	}

	Add(gp1.name, gp1.exec)
	Add(gp2.name, gp1.exec)

	OnStart(func(id string) {
		assert.Equal(t, GetCurrent(), id)
	})

	OnStop(func(name string, current string) {
		if name == gp1.name {
			assert.NotEqual(t, name, current, "A new game should have been started")
		} else {
			assert.Equal(t, name, current, "No new game should have been started")
		}
	})

	Start(gp1.name)
	<-time.After(time.Millisecond * 50)
	Start(gp2.name)

	<-time.After(time.Millisecond * 300)

	Quit()
}

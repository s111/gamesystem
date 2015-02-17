package hub

import (
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestOrigin(t *testing.T) {
	assert.Equal(t, upgrader.CheckOrigin(&http.Request{}), true, "CheckOrigin should always return true")
}

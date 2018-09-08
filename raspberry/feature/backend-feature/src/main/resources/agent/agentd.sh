#!/bin/bash
### BEGIN INIT INFO
# Provides:          agentd
# Required-Start:
# Required-Stop:
# Default-Start:     90 5
# Default-Stop:      20 0 1 2 3 4 6
# Short-Description: agentd initscript
# Description:       Script de servicio para el agente de WSO2 IoT
### END INIT INFO


# Start the service agent
start() {
        cd PATH_DIR
        sleep 60 && sudo PATH_DIR/agentScript.sh &
        ### Create the lock file ###
        touch /var/lock/subsys/agentScript
        echo
}

# Restart the service FOO
stop() {
        PROCESS_PID=`ps aux | grep agentScript.sh | head -n 1 | awk '{print $2}'`
        PYTHON_PID=`ps aux | grep agent.py | head -n 1 | awk '{print $2}'`
        sudo kill $PROCESS_PID 2&>/dev/null
        sudo kill $PYTHON_PID 2&>/dev/null
        ### Now, delete the lock file ###
        rm -f /var/lock/subsys/agentScript
        echo
}

### main logic ###
case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  status)
        status FOO
        ;;
  restart|reload|condrestart)
        stop
        start
        ;;
  *)
        echo $"Usage: $0 {start|stop|restart|reload|status}"
        exit 1
esac

exit 0

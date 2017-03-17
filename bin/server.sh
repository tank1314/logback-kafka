#!/usr/bin/env bash

SERVER_ID=$(pwd | awk -F/ '{print $NF}')
echo $SERVER_ID
start()
{
       VM_OPTS=""
        for line in `cat vm.options`
        do
        VM_OPTS=${VM_OPTS}" "${line}
        done
        echo "Java VM PARAMS:" "${VM_OPTS}"
        nohup java ${VM_OPTS} -classpath conf:lib/* -Dserver_id=$SERVER_ID com.dafy.klog.consumer.KLogConsumerStartup >nohup.out 2>&1 &
        echo $! > pid
}
stop()
{

        PID=""
        for line in `cat pid `
        do
        PID=${line}
        done
        kill -9 ${PID}
}
if [ $1 = "start" ];then
start
elif [ $1 = "stop" ];then
stop
elif [ $1 = "restart"  ];then
stop
sleep 1
start
echo "Restart OK"
else
echo "$1"
echo "Usage server.sh start|stop|restart"
fi
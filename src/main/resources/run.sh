#!/bin/bash

java -XX:+HeapDumpOnOutOfMemoryError -Dlogback.configurationFile=logback.xml -jar wowchat.jar wowchat.conf

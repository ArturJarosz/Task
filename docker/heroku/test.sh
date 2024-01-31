#! /bin/bash

# modules
TASK_SCHEMA="task-schema"
TASK_SAMPLE_DATA="task-sample-data"
TASK_BACKEND="task-backend"

# applications
BACKEND_DYNO="task-test-be"

# dyno types
SCHEMA="schema"
SAMPLE_DATA="sample-data"
WEB="web"

declare -A moduleToApp
declare -A moduleToType
declare -A isOneOffDyno

# defines to which application each of the modules should be deployed
moduleToApp["$TASK_SCHEMA"]="$BACKEND_DYNO"
moduleToApp["$TASK_SAMPLE_DATA"]="$BACKEND_DYNO"
moduleToApp["$TASK_BACKEND"]="$BACKEND_DYNO"

# defines type of the dyno, that should be assigned to the module
moduleToType["$TASK_SCHEMA"]="$SCHEMA"
moduleToType["$TASK_SAMPLE_DATA"]="$SAMPLE_DATA"
moduleToType["$TASK_BACKEND"]="$WEB"

# tells whether dyno is one off type
isOneOffDyno["$TASK_SCHEMA"]="true"
isOneOffDyno["$TASK_SAMPLE_DATA"]="true"
isOneOffDyno["$TASK_BACKEND"]="false"

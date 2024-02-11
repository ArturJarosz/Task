#! /bin/bash

# modules
TASK_SCHEMA="task-schema"
TASK_SAMPLE_DATA="task-sample-data"
TASK_BACKEND="task-backend"
TASK_FRONTEND="task-fe"

# applications
BACKEND_DYNO="task-test-be"
FRONTEND_DYNO="task-test-fe"

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
moduleToApp["$TASK_FRONTEND"]="$FRONTEND_DYNO"

# defines type of the dyno, that should be assigned to the module
moduleToType["$TASK_SCHEMA"]="$SCHEMA"
moduleToType["$TASK_SAMPLE_DATA"]="$SAMPLE_DATA"
moduleToType["$TASK_BACKEND"]="$WEB"
moduleToType["$TASK_FRONTEND"]="$WEB"

# tells whether dyno is one off type
isOneOffDyno["$TASK_SCHEMA"]="true"
isOneOffDyno["$TASK_SAMPLE_DATA"]="true"
isOneOffDyno["$TASK_BACKEND"]="false"
isOneOffDyno["$TASK_FRONTEND"]="false"

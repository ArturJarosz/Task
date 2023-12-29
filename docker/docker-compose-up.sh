# exporting environment arguments so that
export $(xargs < ./env/environment.env)

# TODO: TA-406 run only selected service

docker compose up


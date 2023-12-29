#

# Function that is meant to replace given placeholder with given token. Should be used
# whenever direct injecting token or password value from secrets cannot be used.

replacePlaceholderWithToken() {
    local TOKEN=${1?"Token to repository has to be supplied."}
    local PLACEHOLDER=${2?"Placeholder has to be specified."}
    local FILE_PATH=${3?"File path has to specified."}

    echo "Replacing ${PLACEHOLDER} with provided Token in file ${FILE_PATH}."

    if [ -f "${FILE_PATH}" ]; then
        sed -i "s/$PLACEHOLDER/$TOKEN/" $FILE_PATH
        echo "TOKEN injected"
        exit 0
    else
        echo "File under path ${FILE_PATH} does not exist."
        exit 1
    fi
}


replacePlaceholderWithToken "my_secret_password" "GITHUB_TOKEN_PLACEHOLDER" "./maven/settings.xml"

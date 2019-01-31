docker-compose pull &&

export CONJUR_DATA_KEY="$(docker-compose run --no-deps --rm conjur data-key generate)" &&

# printenv CONJUR_DATA_KEY > dataKey.txt &&

docker-compose up -d &&

echo "Sleeping for 30" &&

sleep 30 &&

export ALL_CONJUR_ADMIN_KEY="$(docker exec conjur_server bash -c 'conjurctl account create quick-start')" &&

export CONJUR_ADMIN_KEY="$(awk -v RS='\r\n'  -F "API key for admin: " '{sub(/ .*/,"",$2);printf $2}' <<<$ALL_CONJUR_ADMIN_KEY)" &&

# printenv CONJUR_ADMIN_KEY > adminKey.txt &&

docker exec conjur_client bash -c 'conjur init -u conjur -a quick-start' &&

docker exec -e CONJUR_ADMIN_KEY conjur_client bash -c 'conjur authn login -u admin -p $CONJUR_ADMIN_KEY' &&

docker exec conjur_client bash -c 'conjur policy load --replace root conjur.yml' &&

export ALL_HOST_API="$(docker exec conjur_client bash -c 'conjur policy load xld xld.yml')" &&

export CONJUR_HOST_KEY="$(awk -v RS='\r\n'  -F "\"api_key\": \"" '{sub(/ .*/,"",$2);gsub("\"","",$2);printf $2}' <<<$ALL_HOST_API)" &&

printenv CONJUR_HOST_KEY > hostKey.txt &&

docker exec conjur_client bash -c 'conjur policy load db db.yml' &&

docker exec -e PASSWORD="secretPassword123" conjur_client bash -c 'conjur variable values add db/password $PASSWORD && \
conjur variable values add db/username "theDBUser" && \
conjur variable values add db/tempPath "/tmp"'

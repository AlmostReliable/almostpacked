#!/bin/bash

TEMP=$(pwd)
DIR=$(dirname $0)
if [[ $DIR = '.' ]]; then
	DIR=$(echo "$TEMP" | awk -F/ '{print $NF}')
else
	cd $DIR
fi

SUB_DIR=False
if [[ ! -f "minecraftinstance.json" ]]; then
	cd ..
	if [[ -f "minecraftinstance.json" ]]; then
		SUB_DIR=True
	else
		echo "Didn't find a valid minecraft instance!"
		exit 1
	fi
fi

echo "Setting up hooks"

echo "Setting up post-merge hook"
echo "#!/bin/sh" > .git/hooks/post-merge
echo "cd $DIR" >> .git/hooks/post-merge
echo "java -jar AlmostPacked.jar -m" >> .git/hooks/post-merge
chmod +x .git/hooks/post-merge

echo "Setting up pre-push hook"
echo "#!/bin/sh" > .git/hooks/pre-push
echo "cd $DIR" >> .git/hooks/pre-push
echo "java -jar AlmostPacked.jar -p" >> .git/hooks/pre-push
chmod +x .git/hooks/pre-push

echo "Done setting up hooks"

echo "Initial tool run"
cd $DIR
java -jar AlmostPacked.jar -m

echo "Please restart your launcher"
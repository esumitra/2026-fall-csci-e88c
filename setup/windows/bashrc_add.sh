#Add these lines to the bashrc
#Fixes highlighting
LS_COLORS=$LS_COLORS:'ow=1;34:'
export LS_COLORS
#Sets these values at beginning of PATH, if not present
#These should already by installed by sdkman
sdkbin="java sbt"
for i in $sdkbin
do
	currpath="$HOME/.sdkman/candidates/$i/current/bin"
	check=`echo $PATH | grep $currpath`
	if [ "$check" = "" ]
	then
		export PATH="$currpath:$PATH"
	fi
done


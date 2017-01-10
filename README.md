# IG Cloner (lab-3/4)
<p>
<b>UPD: </b>
LAB-4;
<br/>
addded Docer file for build/run/test current application in <b>Docker</b> java containcer.	
<br/>
Operation build contain also test executing.
Such as result we can't see opened java desctop UI application but we can see StackTrace trouble desriprion. To my mind issue is related with OS libraries of using UI in it.

<br/>

Also we can use <b>Docker</b> run -v option for receiving builded application and run it on local PC.

<br/>
I use following docker operation for create <b>Docker</b> image/container and start them.
<ul>
	<li>docker build -t mvn .</li>
	<li>docker run --rm --name mvn-tester -it --privileged -e DISPLAY=$DISPLAY mvn</li>
</ul>
</p>

<h3>Required software:</h3>
<ul>
<li>JDK 1.8+</li>
</ul>

<h3>Build using by Maven:</h3>
<ul>
<li>mvn install</li>
</ul>

<h3>Git versions:</h3>
<ul>
<li>git tag -a v1.1 -m </li>
</ul>

<h3>Run tests using by Maven:</h3>
<ul>
<li>mvn test</li>
</ul>

<h3>Travis (system of continuous integration):</h3>
<ul>
<span>Travis</span>
<a href="https://travis-ci.org/Niki-Max-911/lab-3/builds" target="_blank"> build history</a>
</ul>


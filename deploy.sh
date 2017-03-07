javac Clothes.java
javac Detect.java
javac Scores.java
javac NikkiRec.java
sudo cp *.class /usr/share/tomcat6/webapps/ROOT/WEB-INF/classes/
sudo cp dressraw/*.jpg /usr/share/tomcat6/webapps/ROOT/WEB-INF/dressraw
sudo cp topsraw/*.jpg /usr/share/tomcat6/webapps/ROOT/WEB-INF/topsraw
sudo cp bottomsraw/*.jpg /usr/share/tomcat6/webapps/ROOT/WEB-INF/bottomsraw
sudo cp scores/*.csv /usr/share/tomcat6/webapps/ROOT/WEB-INF/scores

sudo cp dress/*.jpg /var/www/html/dress
sudo cp tops/*.jpg /var/www/html/tops
sudo cp bottoms/*.jpg /var/www/html/bottoms

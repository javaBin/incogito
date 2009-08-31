connect 'jdbc:derby:/home/trygvis/dev/com.github/javaBin/incogito/incogito-ems-server/target/ems-home/database/ems/';
update sa.event_timeslot set durationminutes=60 where durationminutes=0;
quit;

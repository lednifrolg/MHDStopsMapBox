<?php
	$response["success"] = 0;
	
	if (empty($_POST))
		die(json_encode($response));

	 if (empty($_POST['long']) || empty($_POST['lat']) || empty($_POST['ref'])) {
		 die(json_encode($response));
	 }
	$dbconn = pg_connect("host='192.168.99.100' port='5432' dbname='gis' user='postgres' password=''");
	
	$long = $_POST['long'];
	$lat = $_POST['lat'];
	$ref = $_POST['ref'];
	

	$query = "with zastavka as (
	select * from planet_osm_point p where public_transport is not null and st_dwithin(p.way::geography, st_makepoint($1, $2), 1000)
	)
select DISTINCT  l.ref ,st_intersects(l.way, z.way), st_asgeojson(z.way), z.osm_id, z.name, z.public_transport, st_distance(st_setsrid(st_makepoint($1, $2), 4326), z.way) as distance from planet_osm_line l, zastavka z
where l.ref = $3 and l.route in ( 'bus', 'tram', 'trolleybus') and st_intersects(l.way, z.way) is true and l.operator = z.operator
order by distance asc limit 1;";
	
	pg_set_client_encoding($dbconn, "UNICODE");
	
	$result = pg_query_params($dbconn, $query, array($long, $lat, $ref));
	
	if (!$result) {
		print "An error occurred.\n";
		exit;
	}
	
	
	$rows = pg_fetch_all($result);
	
	if ($rows) {
		$response["success"] = 1;
		$response["stops"] = array();
		
		foreach ($rows as $row) {
			$stop = array();
			$stop["name"] = $row["name"];
			$stop["id"] = $row["osm_id"];
			$stop["transport"] = $row["public_transport"];
			$stop["link"] = $row["ref"];
			$stop["point"] = $row["st_asgeojson"];
			
			array_push($response["stops"], $stop);
		}
		echo json_encode($response);
	} else {
		print "Failed!";
		die(json_encode($response));
	}
	

	
?>

	
	
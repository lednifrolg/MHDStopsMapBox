<?php
	$response["success"] = 0;
	
	if (empty($_POST))
		die(json_encode($response));

	 if (empty($_POST['ref'])) {
		 die(json_encode($response));
	 }
	$dbconn = pg_connect("host='192.168.99.100' port='5432' dbname='gis' user='postgres' password=''");
	
	$ref = $_POST['ref'];
	

	$query = "select st_asgeojson(way) as geo from planet_osm_line where ref = $1;";
	
	pg_set_client_encoding($dbconn, "UNICODE");
	
	$result = pg_query_params($dbconn, $query, array($ref));
	
	if (!$result) {
		print "An error occurred.\n";
		exit;
	}
	
	
	$rows = pg_fetch_all($result);
	
	if ($rows) {
		$response["success"] = 1;
		$response["routes"] = array();
		
		foreach ($rows as $row) {
			//echo $row['geo'];
			$route = array();
			$route['route'] = $row['geo'];
			
			array_push($response["routes"], $route);
		}
		echo json_encode($response);
	} else {
		die(json_encode($response));
	}
	

	
?>
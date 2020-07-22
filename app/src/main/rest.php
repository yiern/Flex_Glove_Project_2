<?php
	error_reporting(E_ERROR | E_PARSE);
	
	//Connect to MySQL databse
        $mysqli = new mysqli("fdb22.atspace.me", "3187553_yiern", "tigress222", "3187553_yiern");
	$data = $_POST ?? json_decode(file_get_contents('php://input'), true);

	if(count($data) < 1){
		$data = json_decode(file_get_contents('php://input'), true);		
	}
        
        if (mysqli_connect_errno()) {
        echo "Failed to connect to MySQL: " . mysqli_connect_error();
        exit();
        }

        if (mysqli_ping($mysqli)) {
          echo "Connection is ok!";
        } else {
          echo "Error: ". mysqli_error($mysqli);
        }


	
	$action = $data["action"];

	//Use switch case to call different function based on called action
	switch ($action) {
		case 'insert_play_history':
			insertPlayHistory($mysqli);
			break;

		case 'insert_game_history':
			insertGameHistory($mysqli, $data);
			break;

		case 'retrieve_play_times':
			retrievePlayHistory($mysqli);
			break;

		case 'retrieve_game_history':
			retrieveGameHistory($mysqli);
			break;

		default:
			echo json_encode(["status" => 0, "message" => "Invalid Operation"]);
			break;
	}

	//Insert into play history database
	function insertPlayHistory($mysqli)
	{
		//Check if there is any record for today
		//If yes then update
		//else create new record
		$stmt = $mysqli -> prepare('SELECT * FROM play_history WHERE play_date = CURDATE() LIMIT 1');
		$stmt -> execute();
		$result = $stmt -> get_result() -> fetch_assoc();

		$query = "";

		//If no product available, return false and create a new record in the play_history DB
		if(count($result) < 1)
		{
			$query = "INSERT INTO play_history(play_date,times) VALUES (CURDATE(),?)";
			$times = 1;
			$transactionStmt = $mysqli -> prepare($query);		
			$transactionStmt -> bind_param('i',$times);
		}
		//If got record, just increase the count 
		else
		{
			$id = $result["id"];
			$times = $result["times"];
			$times++;

			$query = "UPDATE play_history SET times = ? WHERE id = ?";
			$transactionStmt = $mysqli -> prepare($query);		
			$transactionStmt -> bind_param('ii',$times,$id);
		}

		$result = $transactionStmt -> execute();

		//Successfully add new record
		if(mysqli_affected_rows($mysqli) > 0)
		{
			echo json_encode(["status" => 1]);
		}
		else
		{
			echo json_encode(["status" => 0, "message" => mysqli_error($mysqli), "errorCode" => mysqli_errno($mysqli)]);
		}
	}

	//Retrieve play history
	function retrievePlayHistory($mysqli)
	{
		$stmt = $mysqli -> prepare('SELECT * FROM play_history');
		$stmt -> execute();
		$result = $stmt -> get_result();

		//If no product available, return false
		if(count($result) < 1)
		{
			echo json_encode(["status" => 0, "message" => "No Records"]);			
			return;
		}

		$dataArray = [];

		while($row = $result->fetch_assoc())
		{
			$dataArray[] = $row;
		}

		echo json_encode(["status" => 1, "data" => $dataArray]);
	}
	
	//Insert game history
	function insertGameHistory($mysqli,$data)
	{
		$instruction = $data["instruction"];
		$result = $data["result"];
		$timespent = $data["timespent"];

		//Insert into game_history DB 
		$stmt = $mysqli -> prepare('INSERT INTO game_history(instruction, result, time_spent) VALUES (?,?,?)');
		$stmt -> bind_param('sii',$instruction,$result,$timespent);
		$result = $stmt -> execute();

		//Successfully add new product
		if(mysqli_affected_rows($mysqli) > 0)
		{
			echo json_encode(["status" => 1]);
		}
		else
		{
			echo json_encode(["status" => 0, "message" => mysqli_error($mysqli), "errorCode" => mysqli_errno($mysqli)]);
		}
	}

	//Retrieve game history
	function retrieveGameHistory($mysqli)
	{
		$stmt = $mysqli -> prepare('SELECT * FROM game_history');
		$stmt -> execute();
		$result = $stmt -> get_result();

		//If no product available, return false
		if(count($result) < 1)
		{
			echo json_encode(["status" => 0, "message" => "No Records"]);			
			return;
		}

		$dataArray = [];

		while($row = $result->fetch_assoc())
		{
			$dataArray[] = $row;
		}

		echo json_encode(["status" => 1, "data" => $dataArray]);
	}
?>
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * The sample smart contract for documentation topic:
 * Writing Your First Blockchain Application
 */

package main

/* Imports
 * 4 utility libraries for formatting, handling bytes, reading and writing JSON, and string manipulation
 * 2 specific Hyperledger Fabric specific libraries for Smart Contracts
 */
import (
	//"bytes"
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

// Define the Smart Contract structure
type SmartContract struct {
}

// Define the Tile structure, with 5 properties.  Structure tags are used by encoding/json library
type Tile struct {
	RowIndex     string `json:"rowIndex"`
	ColumnIndex  string `json:"columnIndex"`
	Color        string `json:"color"`
	Points       string `json:"points"`
	LockedBy     string `json:"lockedBy"`
}


/*
 * The Init method is called when the Smart Contract "fabcar" is instantiated by the blockchain network
 * Best practice is to have any Ledger initialization in separate function -- see initLedger()
 */
func (s *SmartContract) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	return shim.Success(nil)
}

/*
 * The Invoke method is called as a result of an application request to run the Smart Contract "fabcar"
 * The calling application program has also specified the particular smart contract function to be called, with arguments
 */
func (s *SmartContract) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {

	// Retrieve the requested Smart Contract function and arguments
	function, args := APIstub.GetFunctionAndParameters()
	// Route to the appropriate handler function to interact with the ledger appropriately
	if function == "queryTile" {
		return s.queryTile(APIstub, args)
	} else if function == "createTile" {
		return s.createTile(APIstub, args)
	} else if function == "deleteTile" {
		return s.deleteTile(APIstub, args)
	} else if function == "lockTile" {
		return s.lockTile(APIstub, args)
	} else if function == "unlockTile" {
		return s.unlockTile(APIstub, args)
	} 
	return shim.Error("Invalid Smart Contract function name.")
}

func (s *SmartContract) queryTile(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	carAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(carAsBytes)
}

func (s *SmartContract) createTile(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	MAX_ROW :=20;
	MAX_COLUMN :=20;

	var logger = shim.NewLogger("myChaincode")
	logger.SetLevel(shim.LogInfo)
	logger.Info("**** Chaincode log starts for CreatingTile *******");

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}

	//Replace tile if exist already. No need to check semantic rules.
	existingTileAsBytes, err := APIstub.GetState(args[0]+args[1])
	
	if err != nil {
		logger.Info("Failed to get tile"+err.Error());
		return shim.Error("Failed to get tile: " + err.Error());
	}

	if existingTileAsBytes != nil {
		
		logger.Info("Replacing tile at "+args[0]+args[1])

		existingTile := Tile{}

		json.Unmarshal(existingTileAsBytes, &existingTile)
		
		//Check if the lockId matches
		if existingTile.LockedBy==args[4]{
		
			var tile= Tile{RowIndex: args[0], ColumnIndex: args[1], Color: args[2], Points: args[3], LockedBy: "Unlocked"}
		
			tileAsBytes, _ := json.Marshal(tile)

			APIstub.PutState(args[0]+args[1], tileAsBytes)

			logger.Info("Tile added with key : "+args[0]+args[1]);
			return shim.Success(nil)

		}else{
			return shim.Error("Failed to CreateTile. Tile "+args[0]+args[1]+" is already locked by lockId"+args[4])
		}		
	}

	// ***************************************************
	//	Check for gravity and base building semantic rules
	// ***************************************************
	row,err:=strconv.Atoi(args[0]);
	if err!=nil{
	 return shim.Error("Error converting row index to number");
	}
	
	column,err:=strconv.Atoi(args[1]);
	if err!=nil{
	 return shim.Error("Error converting column index to number");
	}
	
	
	for i:=row;row<MAX_ROW-1;i++ { 
	
	//Check if any tile exist below
	if isTileExist(APIstub,strconv.Itoa(i+1),args[1]){
		logger.Info("Tile exists at "+strconv.Itoa(i+1)+args[1])
		break;
	}

	//Check for base building if tile doesn't exist in current cell
	logger.Info("Checking for base building..");
	
	//Check if base building exist
	if (column>0 && column<MAX_COLUMN-1 && i<MAX_ROW-1) && isTileExist(APIstub,strconv.Itoa(i+1),strconv.Itoa(column-1)) && isTileExist(APIstub,strconv.Itoa(i+1),strconv.Itoa(column+1)) {	
		logger.Info("Base building exist..");
		break;
	}
	logger.Info("No base building found");

		row++;
	} 
	newRow:=strconv.Itoa(row)
	
	if err!=nil{
	return shim.Error("Error converting new row");
	}
	
	var tile= Tile{RowIndex: newRow, ColumnIndex: args[1], Color: args[2], Points: args[3], LockedBy: "Unlocked"}
	
	tileAsBytes, _ := json.Marshal(tile)

	APIstub.PutState(newRow+args[1], tileAsBytes)

	logger.Info("Tile added with key : "+newRow+args[1]);
	return shim.Success(nil)
}


func (s *SmartContract) deleteTile(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var logger = shim.NewLogger("myChaincode")
	logger.SetLevel(shim.LogInfo)
	logger.Info("**** Chaincode log starts for DeleteTile *******");
	logger.Info("Delete Request for "+args[0]+args[1]);
	var jsonResp string
	var existingTile Tile
	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}
	tileIndex := args[0]+ args[1];

	tileAsBytes, err := APIstub.GetState(tileIndex)
	if err != nil {
		jsonResp = "{\"Error\":\"Failed to get state for " + tileIndex + "\"}"
		return shim.Error(jsonResp)
	} else if tileAsBytes == nil {
		jsonResp = "{\"Error\":\"tile does not exist at " + tileIndex + "\"}"
		return shim.Error(jsonResp)
	}

	err = json.Unmarshal([]byte(tileAsBytes), &existingTile)
	if err != nil {
		jsonResp = "{\"Error\":\"Failed to decode JSON at " + tileIndex + "\"}"
		return shim.Error(jsonResp)
	}

	//Check if the lockId matches
	if existingTile.LockedBy==args[2]{

			err = APIstub.DelState(tileIndex) 
			if err != nil {
				return shim.Error("Failed to delete state:" + err.Error())
			}else{
				logger.Info("Deleted tile from "+tileIndex)
			}
			currentRow,err:=strconv.Atoi(args[0]);
			if err!=nil{
				logger.Info("Current row conversion failed");
			}

			//Check whether any tile exist in the bottom
			for i:=currentRow-1;i>=0;i--{ 
			logger.Info("Retriving tile from "+(strconv.Itoa(i)+args[1]));
			existingTileAsBytes, err := APIstub.GetState(strconv.Itoa(i)+args[1])
			if err != nil {
				logger.Info("Failed to get tile"+err.Error());
				return shim.Error("Failed to get tile: " + err.Error());
			}
			
			if existingTileAsBytes != nil {
				logger.Info("Tile exists at "+strconv.Itoa(i)+args[1])
				tileAsBytes, _ := APIstub.GetState(strconv.Itoa(i)+args[1])
				tile := Tile{}

				json.Unmarshal(tileAsBytes, &tile)
				tile.RowIndex = strconv.Itoa(currentRow);

				tileAsBytes, _ = json.Marshal(tile)
				APIstub.PutState(strconv.Itoa(currentRow)+args[1], tileAsBytes)
				currentRow--;
				}else
				{
					logger.Info("Tile doesn't exist at "+strconv.Itoa(i)+args[1])
					break;
				}
			}
	}else{
		return shim.Error("Cannot delete tile "+args[0]+args[1]+". Tile is locked by different lockId : "+existingTile.LockedBy)
	}   
	
	return shim.Success(nil)
}


func isTileExist(APIstub shim.ChaincodeStubInterface, row string,column string) bool{

	var logger = shim.NewLogger("myChaincode")
	logger.SetLevel(shim.LogInfo)
	logger.Info("Checking if tiles exist at "+row+column);

	tileIndex :=row+column;
	
	tileAsBytes, err := APIstub.GetState(tileIndex)
	if err != nil {
		logger.Info("ERROR : Failed to get state for "+row+column);
		return false;
	} 
	if tileAsBytes == nil {
		logger.Info("Tile doesn't exist");
		return false;

	}
	logger.Info("Tile exist...!!!");

	return true;
}


func (s *SmartContract) lockTile(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var logger = shim.NewLogger("myChaincode")
	logger.SetLevel(shim.LogInfo)
	logger.Info("Trying to obtain lock for tile "+args[0]+args[1]);

	tileIndex :=args[0]+args[1];
	
	tileAsBytes, err := APIstub.GetState(tileIndex)
	if err != nil {
		//jsonResp = "{\"Error\":\"Failed to get state for " + tileIndex + "\"}"
		logger.Info("ERROR : Failed to get state for "+tileIndex);
		return shim.Error("Failed to obtain lock");
	}

	if tileAsBytes != nil {
		
		tile := Tile{}

		json.Unmarshal(tileAsBytes, &tile)
			
			if tile.LockedBy == "Unlocked"{
			tile.LockedBy = args[2];
			}else
			{
				logger.Error("Failed to obtain lock. Tile is already locked by lockId : "+tile.LockedBy);
				return shim.Error("Failed to obtain lock. Tile is already locked by lockId : "+tile.LockedBy);
			}

			tileAsBytes, _ = json.Marshal(tile)
			APIstub.PutState(tileIndex, tileAsBytes)
			logger.Info("Lock Obtained for tile "+tileIndex+" with lockId : "+args[2])
	}else
	{
		logger.Info("No lock obtained since there is no tile exist in "+tileIndex)
	}
	
	return shim.Success(nil);
}


func (s *SmartContract) unlockTile(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	var logger = shim.NewLogger("myChaincode")
	logger.SetLevel(shim.LogInfo)
	logger.Info("unlocking tile at "+args[0]+args[1]);

	tileIndex :=args[0]+args[1];
	
	tileAsBytes, err := APIstub.GetState(tileIndex)
	if err != nil {
		//jsonResp = "{\"Error\":\"Failed to get state for " + tileIndex + "\"}"
		logger.Info("ERROR : Failed to get state for "+tileIndex);
		return shim.Error("Failed to unlock");
	}

	if tileAsBytes != nil {
		
		tile := Tile{}

		//Check if current user is authorized
		json.Unmarshal(tileAsBytes, &tile)
		
		if tile.LockedBy == args[2]{
			tile.LockedBy = "Unlocked";
			}else{
				logger.Error("Failed to unlock. This tile was locked by different lockId : "+tile.LockedBy);
				return shim.Error("Failed to unlock. This tile was locked by different lockId : "+tile.LockedBy);
			}

		tileAsBytes, _ = json.Marshal(tile)
		APIstub.PutState(tileIndex, tileAsBytes)
		
		}
	logger.Info("Tile "+tileIndex+" has been unlocked.")
	return shim.Success(nil);
}

// The main function is only relevant in unit test mode. Only included here for completeness.
func main() {

	// Create a new Smart Contract
	err := shim.Start(new(SmartContract))
	if err != nil {
		fmt.Printf("Error creating new Smart Contract: %s", err)
	}
}

# TFW - Testing Framework
Do you have a bunch of scripts that test applications / services during runtime?

Do you wish to have a unified interface for running those tests instead of using the command line?

Do you require testing evidence?

TFW enables you to do all of this by giving you a unified way of running tests & storing results!


## Tests
Tests are a collection of one or more tasks.

        +-----------+
        | some test |
        +-----+-----+
              |
      +---------------+
      |       |       |
      v       v       v
    task1   task2   task3


A task defines the path to the exectuable (the actual test) and a optional timeout. When TFW runs a test, all of its tasks will be executed and judging by their exit code and runtime as a success or fail.

Example of a test config:

      {
        "settings": {},
        "test": {
          "description": "Example",
          "tasks" : [{
            "name":"task1",
            "path":"scripts/task1.sh",
            "timeout": 1
          },{
            "name":"task2",
            "path":"scripts/task2.sh",
            "timeout": 2
          }]
        }
      }

### Task Results
A task knows two states, passed "true" or "false".
However there are different reasons why a task can fail:
- The task ran longer than the defined timeout and was killed
- The task returned a non zero exit code

If one task failes, the whole test will be marked as failed.


## Test Groups
Test Groups allow to run multiple tests in one go and one report.

                      +-------+
                      | group |
                      +---+---+
                          |
              +-----------+-----------+
              |                       |
        +-----+-----+           +-----+-----+
        | mail test |           | auth test |
        +-----+-----+           +-----+-----+
              |                       |
      +---------------+       +---------------+
      |       |       |       |       |       |
      v       v       v       v       v       v
    task1   task2   task3   auth1   auth2   auth3


## Configuration
Under Windows, TFW will create a folder called "TFW" in your %APPDATA% folder. Under Linux, TFW will create a folder called "/var/lib/TFW". This is later referenced as "base path".
All configuration is saved as JSON files.

### Users
Users are stored in basePath/users.json.
Example of two defined users:

	[
	  {
		"username": "ozzi",
		"password": "BBFA187429F9C089455B8195896DC9EB10FE07AC0BB09954BD23CFD0721E1207ECA457BCF1BBA350E96C42A21C8503B2D6006B731AFE177E84A61F088CD596F2",
		"role": "rw"
	  },{
		"username": "read",
		"password": "BBFA187429F9C089455B8195896DC9EB10FE07AC0BB09954BD23CFD0721E1207ECA457BCF1BBA350E96C42A21C8503B2D6006B731AFE177E84A61F088CD596F2",
		"role": "r"
	  }
	]

The role "r" can only view results, "rw" can additionally run the defined tests.
Passwords are hashed using SHA512 with an prepended salt "TFW_".
You can use the form provided in the web interface to generate said password hashes.
/TFW/frontend/index.html?page=hash

### Tests
All tests are stored in basePath/tests/. Each test is defined in its own file.
The test files need to use the file extension ".test".
Example of a test file called "windows.test". The test name is taken from the filename, hence this tests name is "windows".

	{
	  "settings": {
	  },
	  "test": {
		"description": "Tests Windows",
		"tasks": [{
		  "name": "task1",
		  "path": "script1.bat",
		  "timeout": 10
		  },{
		  "name": "task2",
		  "path": "script2.bat",
		  "timeout": 3
		  },{
		  "name": "task3",
		  "path": "script3.bat",
		  "timeout": 5
		  }]
	  } 
	}

The paths defined can be absolute or relative (to the current directory).

#### Hooks
You can define optional hooks which will be ran if the tests succeeds (successhook) or fails (failurehook).
Example of a test which will, when it runs successfully, execute "sendmail.exe" with a command line argument.

	{
	  "settings": {
		"successhook": "C:\\Program Files (x86)\\Mailer\\sendmail.exe \"windows test succeeded\""
	  },
	  "test": {
		"description": "test_windows",
		"tasks": [{
		  "name": "task1",
		  "path": "script1.bat",
		  "timeout": 1
		  }]
	  } 
	}


### Groups
Groups are stored in basePath/groups/. Every Group is defined in its own file.
The group files need to use the file extension ".group".
Example of a group file called "auth.test". The test name is taken from the filename, hence this tests name is "auth".
This group contains the two tests "auth_sso" and "auth_mail".

	{
	  "description": "Grouping all auth tests",
	  "tests": [{
		"name": "auth_sso"
	  },{
		"name": "auth_mail"
	  }]
	}
	
## Logs
Extensive logs are saved in the basePath/logs folder.

## Web Interface
The web interface is available under (assuming you use Tomcat with default port 8080)
localhost:8080/TFW/frontend/index.html

## API

### /TFW/login
Expects a JSON object with two fields "username" and "password".

      {
        "username" : "ozzi",
        "password" : "letmein"
      }
Returns a JSON object containing the sessionIdentifier which needs to be sent as a header called "X-TFW-Session-ID".

       {
           "username": "ozzi",
           "sessionIdentifier": "1S`Bv@LH{&nD*v:lgXrAZ0z\\n5=q7aJH",
           "role": "rw"
       }

### /TFW/testLogin
Checks if a valid session identifier is sent. Returns either "OK" (200) or "NOK" (403).

### /TFW/logout
Invalidates the current session identifier.

### /TFW/getBasePath
Returns the base path where TFW will look for the test file and will store results.

       /var/lib/TFW/

### /TFW/run/{testname}
Runs a test, returns a handle to the specific test run.
Requires the 'rw' role.

      {"name":"example","handle":"1562594955560"}

### /TFW/runGroup/{groupname}
Runs a test group, returns a handle to the specific test run.
Requires the 'rw' role.

      {"name":"examplegroup","handle":"1562594955561"}

### /TFW/getResults/{testname}
Returns all results for the specific test.
Example:

      [
            {
               "handle":"1560956995338",
               "result":{
                  "testName":"example",
                  "testStartTimestamp":1560956995338,
                  "testStartString":"2019-06-19 17:09:55",
                  "description":"Example",
                  "results":[
                     {
                        "name":"task1",
                        "passed":"true",
                        "description":"Script executed successfully",
                        "output":"TASK 1!!\n",
                        "errorOutput":"",
                        "runTimeInMS":2523,
                        "startTimestamp":1560956996801,
                        "endTimestamp":1560956999400,
                        "startDate":"2019-06-19 17:09:56",
                        "endDate":"2019-06-19 17:09:59"
                     },
                     {
                        "name":"task2",
                        "passed":"true",
                        "description":"Script executed successfully",
                        "output":"task 2\nnewline\n",
                        "errorOutput":"",
                        "runTimeInMS":462,
                        "startTimestamp":1560956999400,
                        "endTimestamp":1560956999863,
                        "startDate":"2019-06-19 17:09:59",
                        "endDate":"2019-06-19 17:09:59"
                     }
                  ]
               }
            },
            {
               "handle":"1559312003986",
               "result":{
                  "testName":"example",
                  "testStartTimestamp":1559312003986,
                  "testStartString":"2019-05-31 16:13:23",
                  "description":"Example",
                  "results":[
                     {
                        "name":"task1",
                        "passed":"true",
                        "description":"Script executed successfully",
                	    . . .
                     }
                  ]
               }
            }
      ]


### /TFW/getGroupResults/{testname}
Returns all results for the specific test group.

	[
	   {
		  "handle":"1559295627973",
		  "result":{
			 "testName":"windowsgroup",
			 "testStartTimestamp":1559295627973,
			 "testStartString":"2019-05-31 11:40:27",
			 "description":"Group Test 'windowsgroup' consisting of tests: windows,windows2,",
			 "results":[
				{
				   "name":"task1",
				   "passed":"true",
				   "description":"Script executed successfully",
				   "output":"\r\nC:\\Users\\ozzi\\Desktop>echo D \r\nD\r\n",
				   "errorOutput":"",
				   "runTimeInMS":45,
				   "startTimestamp":1559295627978,
				   "endTimestamp":1559295628023,
				   "startDate":"2019-05-31 11:40:27",
				   "endDate":"2019-05-31 11:40:28"
				},
				    . . . 
				{
				   "name":"task2",
				   "passed":"false",
				   "description":"Script returned non zero return code \"2\"",
				   "output":"\r\nC:\\Users\\ozzi\\Desktop>echo D \r\nD\r\n\r\nC:\\Users\\ozzi\\Desktop>exit 2 \r\n",
				   "errorOutput":"",
				   "runTimeInMS":38,
				   "startTimestamp":1559295629119,
				   "endTimestamp":1559295629158,
				   "startDate":"2019-05-31 11:40:29",
				   "endDate":"2019-05-31 11:40:29"
				}
			 ]
		  }
	   },{
		. . .
	   }
	]

### /TFW/getLatestResult/{testname}
Returns the latest result for the specific test.

		{
			"testName": "windows",
			"testStartTimestamp": 1562668638877,
			"testStartString": "2019-07-09 12:37:18",
			"description": "test_windows",
			"results": [
				{
					"name": "task1",
					"passed": "true",
					"description": "Script executed successfully",
					"output": "\r\nC:\\WINDOWS\\system32>echo D \r\nD\r\n",
					"errorOutput": "",
					"runTimeInMS": 70,
					"startTimestamp": 1562668638892,
					"endTimestamp": 1562668638970,
					"startDate": "2019-07-09 12:37:18",
					"endDate": "2019-07-09 12:37:18"
				},
				{
					"name": "task2",
					"passed": "true",
					"description": "Script executed successfully",
					"output": "\r\nC:\\WINDOWS\\system32>echo foo \r\nfoo\r\n",
					"errorOutput": "",
					"runTimeInMS": 39,
					"startTimestamp": 1562668638970,
					"endTimestamp": 1562668639021,
					"startDate": "2019-07-09 12:37:18",
					"endDate": "2019-07-09 12:37:19"
				},
				{
					"name": "task3",
					"passed": "true",
					"description": "Script executed successfully",
					"output": "\r\nC:\\WINDOWS\\system32>echo foooo \r\nfoooo\r\n",
					"errorOutput": "",
					"runTimeInMS": 38,
					"startTimestamp": 1562668639023,
					"endTimestamp": 1562668639048,
					"startDate": "2019-07-09 12:37:19",
					"endDate": "2019-07-09 12:37:19"
				}
			]
		}

### /TFW/getLatestGroupResult/{groupname}
Returns the latest result for the specific test group.

	{
		  "handle":"1559295627973",
		  "result":{
			 "testName":"windowsgroup",
			 "testStartTimestamp":1559295627973,
			 "testStartString":"2019-05-31 11:40:27",
			 "description":"Group Test 'windowsgroup' consisting of tests: windows,windows2,",
			 "results":[
				{
				   "name":"task1",
				   "passed":"true",
				   "description":"Script executed successfully",
				   "output":"\r\nC:\\Users\\ozzi\\Desktop>echo D \r\nD\r\n",
				   "errorOutput":"",
				   "runTimeInMS":45,
				   "startTimestamp":1559295627978,
				   "endTimestamp":1559295628023,
				   "startDate":"2019-05-31 11:40:27",
				   "endDate":"2019-05-31 11:40:28"
				},
				    . . . 
				{
				   "name":"task2",
				   "passed":"false",
				   "description":"Script returned non zero return code \"2\"",
				   "output":"\r\nC:\\Users\\ozzi\\Desktop>echo D \r\nD\r\n\r\nC:\\Users\\ozzi\\Desktop>exit 2 \r\n",
				   "errorOutput":"",
				   "runTimeInMS":38,
				   "startTimestamp":1559295629119,
				   "endTimestamp":1559295629158,
				   "startDate":"2019-05-31 11:40:29",
				   "endDate":"2019-05-31 11:40:29"
				}
			 ]
		  }
	   }

### /TFW/getResult/{testname}/{handle}
Returns the test results for the specific test run.

		{
		   "testName":"windows",
		   "testStartTimestamp":1562668638877,
		   "testStartString":"2019-07-09 12:37:18",
		   "description":"test_windows",
		   "results":[
			  {
				 "name":"task1",
				 "passed":"true",
				 "description":"Script executed successfully",
				 "output":"\r\nC:\\WINDOWS\\system32>echo D \r\nD\r\n",
				 "errorOutput":"",
				 "runTimeInMS":70,
				 "startTimestamp":1562668638892,
				 "endTimestamp":1562668638970,
				 "startDate":"2019-07-09 12:37:18",
				 "endDate":"2019-07-09 12:37:18"
			  },
			  {
				 "name":"task2",
				 "passed":"true",
				 "description":"Script executed successfully",
				 "output":"\r\nC:\\WINDOWS\\system32>echo foo \r\nfoo\r\n",
				 "errorOutput":"",
				 "runTimeInMS":39,
				 "startTimestamp":1562668638970,
				 "endTimestamp":1562668639021,
				 "startDate":"2019-07-09 12:37:18",
				 "endDate":"2019-07-09 12:37:19"
			  },
			  {
				 "name":"task3",
				 "passed":"true",
				 "description":"Script executed successfully",
				 "output":"\r\nC:\\WINDOWS\\system32>echo foooo \r\nfoooo\r\n",
				 "errorOutput":"",
				 "runTimeInMS":38,
				 "startTimestamp":1562668639023,
				 "endTimestamp":1562668639048,
				 "startDate":"2019-07-09 12:37:19",
				 "endDate":"2019-07-09 12:37:19"
			  }
		   ]
		}

### /TFW/getGroupResult/{groupname}/{handle}
Returns the test group results for the specific test run.

	{
	   "testName":"windowsgroup",
	   "testStartTimestamp":1562589478619,
	   "testStartString":"2019-07-08 14:37:58",
	   "description":"Group Test 'windowsgroup' consisting of tests: windows,windows2,",
	   "results":[
		  {
			 "name":"task1",
			 "passed":"true",
			 "description":"Script executed successfully",
			 "output":"\r\nC:\\WINDOWS\\system32>echo D \r\nD\r\n",
			 "errorOutput":"",
			 "runTimeInMS":45,
			 "startTimestamp":1562589478621,
			 "endTimestamp":1562589478655,
			 "startDate":"2019-07-08 14:37:58",
			 "endDate":"2019-07-08 14:37:58"
		  },
		  {
			 "name":"task2",
			 "passed":"true",
			 "description":"Script executed successfully",
			 "output":"\r\nC:\\WINDOWS\\system32>echo fo \r\nfo\r\n",
			 "errorOutput":"",
			 "runTimeInMS":41,
			 "startTimestamp":1562589478655,
			 "endTimestamp":1562589478709,
			 "startDate":"2019-07-08 14:37:58",
			 "endDate":"2019-07-08 14:37:58"
		  },
		  {
			 "name":"task3",
			 "passed":"true",
			 "description":"Script executed successfully",
			 "output":"\r\nC:\\WINDOWS\\system32>echo fooo \r\nfooo\r\n",
			 "errorOutput":"",
			 "runTimeInMS":35,
			 "startTimestamp":1562589478709,
			 "endTimestamp":1562589478745,
			 "startDate":"2019-07-08 14:37:58",
			 "endDate":"2019-07-08 14:37:58"
		  },
		  {
			 "name":"task1",
			 "passed":"false",
			 "description":"Script execution longer than timeout",
			 "output":"\r\nC:\\WINDOWS\\system32>echo fo \r\nfo\r\n\r\nC:\\WINDOWS\\system32>choice /t 10 /C JN /CS /D J \r\n[J,N]?",
			 "errorOutput":"",
			 "runTimeInMS":1016,
			 "startTimestamp":1562589478745,
			 "endTimestamp":1562589479762,
			 "startDate":"2019-07-08 14:37:58",
			 "endDate":"2019-07-08 14:37:59"
		  },
		  {
			 "name":"task2",
			 "passed":"false",
			 "description":"Script returned non zero return code \"2\"",
			 "output":"\r\nC:\\WINDOWS\\system32>echo D \r\nD\r\n\r\nC:\\WINDOWS\\system32>exit 2 \r\n",
			 "errorOutput":"",
			 "runTimeInMS":33,
			 "startTimestamp":1562589479762,
			 "endTimestamp":1562589479797,
			 "startDate":"2019-07-08 14:37:59",
			 "endDate":"2019-07-08 14:37:59"
		  }
	   ]
	}

### /TFW/getStatus/{testname}/{handle}
Returns the current state of a specifc test run.

	{"state":"running"}

Two states exist, "running" or "done".

### /TFW/getGroupStatus/{groupname}/{handle}
Returns the current state of a specifc test group run.

	{"state":"running"}

Two states exist, "running" or "done".

### /TFW/getTestList
Returns all tests.

      [{"name":"mail"},{"name":"auth"}]

### /TFW/getTestGroupList
Returns all test groups.

	[{"name":"windowsgroup","tests":[{"name":"windows"},{"name":"windows2"}]},{"name":"authgroup","tests":[{"name":"auth"},{"name":"mail"}]}]

### /TFW/reload
Reloads the users.json file.
Requires the 'rw' role.

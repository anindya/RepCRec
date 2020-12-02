The project uses `gradle` for package management and can be reproduced using gradle. 

To replicates results without worrying about setup :
You can use reprounzip-docker, if you don’t have it installed please run
`pip install reprounzip-docker`

### Steps to run :
1. `reprounzip docker setup ac8184_rm5310_proj.rpz ./RepCRec`
2. To move your input into docker file run `reprounzip docker upload ./RepCRec <inputFilePath>:arg3`
3. `reprounzip docker run ./RepCRec` : This will generate a file out.txt at the the same path as the input file. The logs can be seen on the terminal window.
4. To download the output file run `reprounzip docker download ./RepCRec arg4:myout.txt`. This will create a `myout.txt` file in your current directory.

## Assumptions
1. Only one input for a transaction would be in the pipeline.
2. End instruction only comes when the transaction can really be committed/aborted i.e. all its instructions should be executed or have failed by now.

## Some details:
1. The system has aggressive deadlock detection and abortion. If at any point multiple deadlocks exist, they will all be undone before moving ahead. This could lead to slow performance at some points.

# Packages :
The following is a brief summary of each Package and some of the constituent classes. Please look at code comments for more details.

1.  Instructions : This packages hold the classes to read data from IOUtil class and parse inputLines. Each input maps to one of the designated running classes which extend the `Instruction` class and each of them have their own way of execution of the request that comes their way.
2. Transactions : handles all transaction related requests and data.
	1. TransactionManager:  The central location to talk to each of the transactions. Maintains the currentWaiting instructions for each of the dataItem names and is also responsible for working with deadlock detector and aborting any transaction based on YoungestAbort technique
3. Sites : This package has all the information related to the different sites.
	1. SiteManager : singleton class which is tightly coupled with the TransactionManager and the two in conjunction work for the InstructionManager to execute the incoming requests.
	2. Site : Individual site classes which contain @dataManagerImpl objects.
4. DataManager : individual bits of data and their management is done by this package. Contains:
	1. DataItem : building block of the database. Can be considered as a single row of the database which also contains its past version based on time of commit.
	2. DataManagerImpl : Each sites has an instance to this class to work with. These are responsible for holding the list of dataItems on the site, the status of recovery of each of the replicated dataItems and the lockTable instances.
5. Locks : Contain lockManagement based on lockTable
	1. LockTable : class responsible for holding the locks on a given site and deciding what to do with incoming lockAcquire requests.
6. DeadlockManager : responsible for reading lockTable data of all sites via TransactionManager and deciding if a deadlock exists or not. Contains YoungestAbort technique for creating a deadlockRecommendation to break a deadlock.


Libraries used :
* [JgraphT](https://github.com/jgrapht/jgrapht) Dimitrios Michail, Joris Kinable, Barak Naveh, and John V. Sichi. 2020. JGraphT—A Java Library for Graph Data Structures and Algorithms. ACM Trans. Math. Softw. 46, 2, Article 16.  
*  [Lombok](https://github.com/rzwitserloot/lombok)  
* [apache-commons-lang](https://commons.apache.org/proper/commons-lang/)  
* [slf4j](http://www.slf4j.org/)  
### Building project 
1. execute `mvn package` and copy the jar `hdfs-debugger-1.0-SNAPSHOT.jar`  from `target` dir  to `lib` 
2. execute  `./DownloadDependency.sh` this will download  hadoop client dependencies to `lib` dir

### Run file 
replace the values with in `<>`  in `start.sh` file  `<principal>` `<keytab>` `<path eg /warehouse>` 
Run `./start.sh `

This will run java class Test  to retrieve list of dirs mentioned in the args with hadoop jar dependencies added to class path.

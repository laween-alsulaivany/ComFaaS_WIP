
# CloudEdgeFaaSTesting

Pure java client server software that helps automate the testing of java based FaaS software testing.  


## Deployment

To deploy this project, move a copy of the Server Folder into the servers directory and use this command.

port - this is up to you and will need to be the same for the client. please be sure the port is open for the server.

```bash
  javac MainS.java ; java MainS [port]
```

For the edge computer, move the Client Folder in to the edge directory and use this command.

ip address - the ip address of the server.

```bash
    javac MainC.java ; java MainC [ip address] [port]
```
## Documentation



To properly use the softare, first set up the Server File and load the programs folder with the wanted pure java programs to use.

!!! Program option are hard written into the softare at this time, future improvements will be made to circumvent this. !!!

After set up, use the command above and start the server, it wont prompt anything until the client connects.

As for the client, the Client Folder needs to be moved to the edge and initialized with following command. At this time, the input folder only supports jpg's to be processed.

A message on both terminals will display then the two programs are connected to eachother. 
## Authors

- [@judahn02](https://github.com/judahn02)
- Jinu Lee
- Hanku Lee
## Contributing

Contributions are always welcome!



Please adhere to this project's `code of conduct`.


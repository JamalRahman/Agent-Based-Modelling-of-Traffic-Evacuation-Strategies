finalFile = open("SiouxFallsNetworkFull.txt","w+")
nodesFile = open("config_data/SiouxFalls_nodes.txt","r")
edgesFile = open("config_data/SiouxFalls_net.txt","r")

finalFile.write("<NODES>\n")
for line in nodesFile:
    inputLine = line.split("\t")
    inputLine = inputLine[1:-1]
    x = int(inputLine[0])
    y = int(inputLine[1])
    x = str(x-50000)
    y = str((510000) - y)
    newLine = []
    newLine.append(x)
    newLine.append(y)
    newLine.append(str(0))
    newLine.append(str(0))

    newLine = (" ").join(newLine)
    finalFile.write(newLine+"\n")

finalFile.write("<EDGES>\n")

for line in edgesFile:
    if(line[0]=="<" or line[0]=="~"):
        continue
    inputLine = line.split("\t")
    if(inputLine[0]=="\n"):
        continue
    inputLine = inputLine[1:-1]
    lineData = []
    lineData.append(inputLine[0])
    lineData.append(inputLine[1])
    lineData.append(str(round(float(inputLine[3])*1609)))
    newLine = (" ".join(lineData))
    finalFile.write(newLine+"\n")

finalFile.close()
nodesFile.close()
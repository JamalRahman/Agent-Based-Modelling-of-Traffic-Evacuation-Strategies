finalFile = open("bham.txt","w+")
nodesFile = open("bhamNodes.txt","r")
edgesFile = open("bhamNet.txt","r")

finalFile.write("<NODES>\n")
for line in nodesFile:
    inputLine = line.split()
    inputLine = inputLine[1:]
    x = int(round(float(inputLine[0])))
    y = int(round(float(inputLine[1])))
    newLine = []
    newLine.append(str(x))
    newLine.append(str(y))
    newLine.append(str(0))
    newLine.append(str(0))
    newLine = (" ").join(newLine)
    finalFile.write(newLine+"\n")

finalFile.write("<EDGES>\n")
print("Edges now")

for line in edgesFile:
    if(line[0]=="<" or line[0]=="~"):
        continue
    inputLine = line.split()

    if(len(inputLine)==0):
        continue
    if(inputLine[0]=="\n"):
        continue
    lineData = []
    lineData.append(inputLine[0])
    lineData.append(inputLine[1])
    lineData.append(str(round(float(inputLine[3])*1609)))
    newLine = (" ".join(lineData))
    finalFile.write(newLine+"\n")

finalFile.close()
nodesFile.close()
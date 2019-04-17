import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import cm
from matplotlib.colors import ListedColormap, LinearSegmentedColormap
import matplotlib.colors as mc
import numpy as np
from scipy.interpolate import griddata
from scipy import interpolate
import re
import copy 

def main():     
    lines = [line.rstrip() for line in open('TestNetA_throttling_thresholds.txt')]

    data = []
    datapoint=[]
    baseValues = []
    averageBaseValue = None
    currentList = []
    for line in lines:
        if line=="BASE":
            currentList = baseValues
        elif line.isdigit():
            currentList.append(int(line))
        else:
            datapoint.append(currentList)
            data.append(datapoint)
            thresholds = re.findall(r"\d+\.\d+", line)
            currentUT = float(thresholds[0])
            currentLT = float(thresholds[1])
            currentList = []
            datapoint = []
            datapoint.append(currentLT)
            datapoint.append(currentUT)

    datapoint.append(currentList)
    data.append(datapoint)
    baseValues = data[0][0]
    del[data[0]]

    baseAverage = np.mean(baseValues)
    dataCoordinates = []

    for i in range(0,len(data)):
        values = data[i][2]
        average = np.mean(values)
        percentageImprovement = 100*(1-(average/baseAverage))
        percentageImprovement = round(percentageImprovement,0)
        if(percentageImprovement<0):
            percentageImprovement = 0
        datapoint = []
        datapoint.append(data[i][0])
        datapoint.append(data[i][1])
        datapoint.append(percentageImprovement)
        dataCoordinates.append(datapoint)
    print(dataCoordinates)

    a = np.array(dataCoordinates)

    x = a[:,0]
    y = a[:,1]
    z = a[:,2]

    xi = np.linspace(0,1,100)
    yi = np.linspace(0,1,100)
    zi = griddata((x, y), z, (xi[None,:], yi[:,None]), method='linear')


    test = [0,0.1,5,10,15,25,35,47]

    r = [0,0,0.25,0.92,1,0.91,0.5,0]
    g = [0,0,0.9,1,0.67,0,0,0]
    b = [0.5,0.5,1,0.04,0,0,0,0]
    
    f_red = interpolate.interp1d(test,r)
    f_green = interpolate.interp1d(test,g)
    f_blue = interpolate.interp1d(test,b)
    vals = []
    for i in range(0,45):
        entry = [f_red(i),f_green(i),f_blue(i),1]
        vals.append(entry)

    newColor = ListedColormap(vals)


    plt.contour(xi,yi,zi,levels=32,colors='k',linewidths=0.2)
    plt.contourf(xi,yi,zi,levels=128,cmap=newColor)
    plt.xticks(x)
    plt.yticks(y)
    plt.xlabel("Lower Threshold")
    plt.ylabel("Upper Threshold")
    colorbar = plt.colorbar(ticks=range(0,int(np.max(z)),5))

    colorbar.set_label("Percentage Improvement over no throttling")
    plt.show()

    # plt.tricontour(x,y,z,levels=10,colors='k',linewidths=0.1)
    # plt.tricontourf(x,y,z,levels=10,cmap='jet')
    # plt.colorbar()
    # plt.show()

    
        # IF BASE
            # form array of values
            # calculate average base value
        # If [UT,LT]
            # Form list dataset = []
            # dataset append LT and UT: [LT,UT]
            
        # if number
            # add number to current list of values
            # find average for this list
            # dataset append list [LT,UT,[VALUES]]
            # datapoint = [LT,UT,avgClearanceTime]





if __name__ == "__main__":
    main()

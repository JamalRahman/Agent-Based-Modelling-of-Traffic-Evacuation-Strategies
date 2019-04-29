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
import sys
import matplotlib.ticker as mticker

def parseFile(file):

    lines = [line.rstrip() for line in open(file)]

    data = []
    datapoint=[]
    currentList = []
    for line in lines:
        if line.isdigit():
            currentList.append(int(line))
        else:
            datapoint.append(currentList)
            data.append(datapoint)
            values = re.findall(r"\d+", line)
            currentX = float(values[0])
            currentList = []
            datapoint = []
            datapoint.append(currentX)

    datapoint.append(currentList)
    data.append(datapoint)
    if(len(data[0][0])==0):
        del[data[0]]
    a = np.array(data)

    return a

def main():     
    mainArray = parseFile(sys.argv[1])


    xs = mainArray[:,0]
    raw_ys = mainArray[:,1]
    average_ys = [np.mean(i) for i in raw_ys]
    std_dev_ys = [np.std(i) for i in raw_ys]
    sqrt_ys = [np.sqrt(i) for i in average_ys]
    fig, ax1 = plt.subplots()
    ax1.plot(xs,average_ys,linewidth=0.8,ms=3)
    
    ax1.minorticks_on()
    ax1.tick_params(axis='x', which='minor', bottom=False)
    ax1.set_xlabel("Population")
    ax1.set_ylabel("Average step simulation time (seconds / step)")
    
    plt.show()

if __name__ == "__main__":
    main()

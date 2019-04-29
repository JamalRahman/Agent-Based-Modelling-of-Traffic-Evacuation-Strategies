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
            values = re.findall(r"\d+\.\d+", line)
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
    baseValues = parseFile(sys.argv[2])
    baseValues = baseValues[0,0]
    baseAverage = np.mean(baseValues)
    baseStd = np.std(baseValues)


    xs = mainArray[:,0]
    raw_ys = mainArray[:,1]
    average_ys = [np.mean(i) for i in raw_ys]
    std_dev_ys = [np.std(i) for i in raw_ys]
    percentageImprovement_ys = [(100*(1-(value/baseAverage))) for value in average_ys]

    fig, ax1 = plt.subplots()
    ax1.errorbar(xs,average_ys,std_dev_ys,fmt='-o',linewidth=0.8,capsize=5,ms=3)
    ax1.axhline(y=baseAverage, ls='dashed',color='r',label='Clearance Time without greed')
    ax1.minorticks_on()
    ax1.set_xticks(list(xs))
    ax1.tick_params(axis='x', which='minor', bottom=False)
    ax1.set_xlabel("Greed Threshold")
    ax1.set_ylabel("Clearance time (steps)")
    
    ax2 = ax1.twinx()
    ax2.set_ylabel("% Improvement on Base Clearance Time")
    ax2.set_ylim(ax1.get_ylim())

    formatter = mticker.FuncFormatter(lambda u, pos: '{:.1f}'.format(100*(1-(u/baseAverage))))
    ax2.yaxis.set_major_formatter(formatter)
    plt.show()

if __name__ == "__main__":
    main()

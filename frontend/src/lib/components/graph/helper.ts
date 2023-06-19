
/**
 * The function creates graph data from sensor data.
 * @param {any} data - The data parameter is an array of objects representing sensor data. Each object
 * contains information about the sensor type, unit, and an array of values with timestamps and
 * corresponding sensor readings.
 * @returns The function `createGraphData` returns an object containing graph data for each sensor in
 * the input data. The keys of the object are the sensor types and the values are objects containing
 * the labels and datasets for the graph.
 */
export function createGraphData(data: any) {
  let graphData: any = {};
  for (let sensor of data) {
    let sensorType = sensor.sensorType;
    let sensorUnit = sensor.sensorUnit;
    let labels: any = [];
    let datasets: any = [];

    let sensorLimits = sensor.sensorLimits || [];

    // make sure that the values are sorted by timestamp
    sensor.values.sort((a, b) => {
      return new Date(a.timeStamp).getTime() - new Date(b.timeStamp).getTime();
    });
    // make sure that the limits are sorted by timestamp
    sensor.sensorLimits.sort((a, b) => {
      return new Date(a.timeStamp).getTime() - new Date(b.timeStamp).getTime();
    });

    // get the start and end time of the values
    const startTime = new Date(sensor.values[0].timeStamp).getTime();
    const endTime = new Date(
      sensor.values[sensor.values.length - 1].timeStamp
    ).getTime();
    // get all timestamps of the values and limits
    const timeStamps = [
      ...sensor.values.map((data) => new Date(data.timeStamp)),
      ...sensor.sensorLimits.map((data) => new Date(data.timeStamp)),
    ];

    timeStamps.sort((a, b) => a.getTime() - b.getTime());

    // Remove duplicate timestamps (if any)
    const uniqueTimestamps = Array.from(new Set(timeStamps));

    // Create the labels array with the timestamps
    labels = uniqueTimestamps;

    // create SensorDataSet
    //---------------------------------------------------------------
    let sensorData: any = [];
    let pointBackgroundColor: any = [];
    for (let data of sensor.values) {
      sensorData.push(data.value);

      if (data.aboveLimit || data.belowLimit) {
        pointBackgroundColor.push("rgba(255,0,0,1)");
      } else {
        pointBackgroundColor.push("rgba(75,192,192,1)");
      }
    }

    let sensorValueSet = {
      label: sensorType,
      fill: true,
      lineTension: 0.5,
      backgroundColor: "rgba(75,192,192,0.5)",
      borderColor: "rgba(75,192,192,1)",
      pointBackgroundColor: pointBackgroundColor,
      pointRadius: 4,
      pointHoverRadius: 4,
      xAxisID: "x",
      yAxisID: "y",
      data: sensorData,
    };
    datasets.push(sensorValueSet);
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    if (sensorLimits.length > 0) {
      let upperLimitData: any = [];
      let lowerLimitData: any = [];
      labels.map((label) => {
        sensorLimits.map((limit) => {
          if (label.getTime() === new Date(limit.timeStamp).getTime()) {
            upperLimitData.push(limit.upperLimit);
            lowerLimitData.push(limit.lowerLimit);
          }
        });
        let newestLimit = sensorLimits[sensorLimits.length - 1];
        if (label.getTime() > new Date(newestLimit.timeStamp).getTime()) {
          upperLimitData.push(newestLimit.upperLimit);
          lowerLimitData.push(newestLimit.lowerLimit);
        }

        upperLimitData.push(null);
        lowerLimitData.push(null);
      });
      // let upperLimitData = sensorLimits.map(data => {

      //   const index = labels.findIndex((label) => label.getTime() === new Date(data.timeStamp).getTime());
      //   return index !== -1 ? data.upperLimit : 0;
      // })
      let upperLimit = {
        label: "UpperLimit",
        fill: false,
        pointRadius: 0,
        lineTension: 0.5,
        backgroundColor: "rgba(255,0,0,1)",
        borderColor: "rgba(255,0,0,1)",
        data: interpolateMissingValues(upperLimitData),
      };
      datasets.push(upperLimit);

      // let lowerLimitData = sensorLimits.map(data => {
      //   const index = labels.findIndex(label => label.getTime() === new Date(data.timeStamp).getTime());
      //   return index !== -1 ? data.lowerLimit : 0;
      // });
      let lowerLimit = {
        label: "LowerLimit",
        fill: false,
        pointRadius: 0,
        lineTension: 0.5,
        backgroundColor: "rgba(0,0,255,1)",
        borderColor: "rgba(0,0,255,1)",
        data: interpolateMissingValues(lowerLimitData),
      };
      datasets.push(lowerLimit);
    }
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    let outsideLimits = {
      label: "Outside limits",
      backgroundColor: "rgba(255,0,0,1)",
    };
    datasets.push(outsideLimits);
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    //---------------------------------------------------------------
    graphData[sensorType] = {
      // labels: sensor.values.map((value: any) =>
      //   new Date(value.timeStamp).toLocaleString("de-DE")
      // ),
      labels: labels.map((label) => label.toLocaleString("de-DE")),
      datasets: datasets,
    };
  }
  return graphData;
}

function interpolateMissingValues(data) {
  let interpolatedData = [];
  let lastValue = null;
  for (let i = 0; i < data.length; i++) {
    if (data[i] === null) {
      if (lastValue === null) {
        interpolatedData.push(null);
      } else {
        let nextValue = null;
        let j = i + 1;
        while (nextValue === null && j < data.length) {
          nextValue = data[j];
          j++;
        }
        if (nextValue === null) {
          interpolatedData.push(null);
        } else {
          interpolatedData.push((lastValue + nextValue) / 2);
        }
      }
    } else {
      interpolatedData.push(data[i]);
      lastValue = data[i];
    }
  }

  return interpolatedData;
}

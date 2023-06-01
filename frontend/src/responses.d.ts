declare namespace Responses {
  export interface GrantedAuthority {
    authority: string;
  }

  export interface Person {
    Person: {
      username: string;
      password: string;
      /** Format: uuid */
      token: string;
      permissions: GrantedAuthority[];
      email: string;
      sensorStationPersonReferences: SensorStationPersonReference[];
      /** Format: uuid */
      personId: string;
    };
  }

  export interface SensorStationPersonReference {
    /** Format: uuid */
    id: string;
    inDashboard: boolean;
    assigned: boolean;
    deleted: boolean;
  }

  export interface Sensor {
    /** Format: uuid */
    sensorId: string;
    type: string;
    unit: string;
  }

  export interface SensorLimits {
    /** Format: date-time */
    timeStamp: string;
    /** Format: float */
    upperLimit: number;
    /** Format: float */
    lowerLimit: number;
    /**
     * Format: int32
     * @description in seconds
     */
    thresholdDuration: number;
    sensor: Sensor;
    deleted: boolean;
  }

  export interface MessageResponse {
    message: string;
  }

  export interface AccessPoint {
    /** Format: uuid */
    deviceId: string;
    /** Format: uuid */
    selfAssignedId: string;
    roomName: string;
    /** Format: int32 */
    transferInterval: number;
    scanActive: boolean;
    /** Format: uuid */
    accessToken: string;
    sensorStations: SensorStation[];
    /** Format: date-time */
    lastConnection: string;
    username: string;
    authorities: GrantedAuthority[];
    password: string;
    connected: boolean;
    unlocked: boolean;
    deleted: boolean;
  }

  export interface SensorData {
    /** Format: date-time */
    timeStamp: string;
    /** Format: float */
    value: number;
    alarm: string;
    aboveLimit: boolean;
    belowLimit: boolean;
    sensor: Sensor;
    deleted: boolean;
  }

  export interface SensorStation {
    /** Format: uuid */
    deviceId: string;
    bdAddress: string;
    name: string;
    /** Format: int32 */
    dipSwitchId: number;
    sensorData: SensorData[];
    sensorLimits: SensorLimits[];
    sensorStationPersonReferences: SensorStationPersonReference[];
    sensorStationPictures: SensorStationPicture[];
    authorities: GrantedAuthority[];
    password: string;
    connected: boolean;
    username: string;
    unlocked: boolean;
    deleted: boolean;
  }

  export interface SensorStationPicture {
    /** Format: uuid */
    pictureId: string;
    picturePath: string;
    /** Format: date-time */
    timeStamp: string;
  }

  export interface CreatedUserResponse {
    id: string;
    username: string;
    token: string;
    permissions: GrantedAuthority[];
  }

  export interface TokenResponse {
    /** Format: uuid */
    token: string;
  }

  export interface LoginResponse {
    token: string;
    personId: string;
    permissions: GrantedAuthority[];
  }

  export interface PermissionResponse {
    permissions: GrantedAuthority[];
  }

  export interface InnerResponse {
    /** Format: uuid */
    sensorStationId: string;
    bdAddress: string;
    roomName: string;
    name: string;
    /** Format: int32 */
    transferInterval: number;
    gardener: Person;
    /** Format: int32 */
    dipSwitchId: number;
    unlocked: boolean;
    connected: boolean;
    deleted: boolean;
  }

  export interface SensorStationsResponse {
    sensorStations: InnerResponse[];
  }

  export interface SensorStationResponse {
    sensorStation: InnerResponse;
  }

  export interface InnerPlantPicture {
    pictureId: string;
    timeStamp: string;
  }

  export interface PlantPicturesResponse {
    pictures: InnerPlantPicture[];
    roomName: string;
    plantName: string;
  }

  export interface SensorStationPublicInfo {
    name: string;
    roomName: string;
  }

  export interface TimeStampedSensorData {
    timeStamp: string;
    value: number;
    alarm: string;
    aboveLimit: boolean;
    belowLimit: boolean;
  }

  export interface InnerSensors {
    sensorType: string;
    sensorUnit: string;
    values: TimeStampedSensorData[];
    sensorLimits: SensorLimits[];
  }

  export interface SensorStationDataResponse {
    data: InnerSensors[];
    sensorStationId: string;
  }

  export interface DashboardSensorStation {
    name: string;
    roomName: string;
    sensorStationId: string;
    pictureIds: string[];
    connected: boolean;
    unlocked: boolean;
    deleted: boolean;
  }

  export interface DashBoardDataResponse {
    sensorStations: DashboardSensorStation[];
  }

  export interface ListResponse {
    items: any[];
  }

  export interface AdminSensorStationResponse {
    sensorStations: InnerResponse[];
  }

  export interface InnerAccessPoint {
    accessPointId: string;
    selfAssignedId: string;
    roomName: string;
    unlocked: boolean;
    scanActive: boolean;
    connected: boolean;
    transferInterval: number;
    sensorStations: AdminSensorStationResponse;
  }

  export interface AccessPointListResponse {
    accessPoints: InnerAccessPoint[];
  }

  export interface Limits {
    lowerLimit: number;
    upperLimit: number;
  }

  export interface SensorInfo {
    sensorName: string;
    limits: Limits;
    alarmThresholdTime: number;
  }

  export interface SensorStationInfo {
    bdAddress: string;
    sensor: SensorInfo[];
  }

  export interface AccessPointConfigResponse {
    roomName: string;
    pairingMode: boolean;
    transferInterval: number;
    sensorStations: SensorStationInfo;
  }
}

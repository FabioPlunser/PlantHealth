declare namespace Responses {
  export interface GrantedAuthority {
    authority: string;
  }

  export interface Person {
    username: string;
    password: string;
    /** Format: uuid */
    token: string;
    permissions: GrantedAuthority[];
    email: string;
    sensorStationPersonReferences: SensorStationPersonReference[];
    /** Format: uuid */
    personId: string;
  }

  export interface SensorStationPersonReference {
    /** Format: uuid */
    id: string;
    inDashboard: boolean;
    assigned: boolean;
    deleted: boolean;
  }

  export interface MessageResponse {
    message: string;
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
    /** Format: int32 */
    thresholdDuration: number;
    sensor: Sensor;
    deleted: boolean;
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

  export interface AuthenticationException {
    cause: object;
    stackTrace: any[];
    message: string;
    suppressed: any[];
    localizedMessage: string;
  }

  export interface TokenExpiredException {
    cause: object;
    stackTrace: any[];
    message: string;
    suppressed: any[];
    localizedMessage: string;
  }

  export interface AccessDeniedException {
    cause: object;
    stackTrace: any[];
    message: string;
    suppressed: any[];
    localizedMessage: string;
  }

  export interface CreatedUserResponse {
    /** Format: uuid */
    id: string;
    username: string;
    /** Format: uuid */
    token: string;
    permissions: GrantedAuthority[];
  }

  export interface TokenResponse {
    /** Format: uuid */
    token: string;
  }

  export interface LoginResponse {
    /** Format: uuid */
    token: string;
    /** Format: uuid */
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
    /** Format: uuid */
    pictureId: string;
    /** Format: date-time */
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

  export interface InnerSensors {
    sensorType: string;
    sensorUnit: string;
    values: TimeStampedSensorData[];
    sensorLimits: SensorLimits[];
  }

  export interface SensorStationDataResponse {
    data: InnerSensors[];
    /** Format: uuid */
    sensorStationId: string;
  }

  export interface TimeStampedSensorData {
    /** Format: date-time */
    timeStamp: string;
    /** Format: double */
    value: number;
    alarm: string;
    aboveLimit: boolean;
    belowLimit: boolean;
  }

  export interface DashBoardDataResponse {
    sensorStations: DashboardSensorStation[];
  }

  export interface DashboardSensorStation {
    name: string;
    roomName: string;
    /** Format: uuid */
    sensorStationId: string;
    pictureIds: any[];
    connected: boolean;
    unlocked: boolean;
    deleted: boolean;
  }

  export interface ListResponse {
    items: any[];
  }

  export interface AdminSensorStationsResponse {
    sensorStations: InnerResponse[];
  }

  export interface AccessPointListResponse {
    accessPoints: InnerAccessPoint[];
  }

  export interface InnerAccessPoint {
    /** Format: uuid */
    accessPointId: string;
    /** Format: uuid */
    selfAssignedId: string;
    roomName: string;
    unlocked: boolean;
    scanActive: boolean;
    connected: boolean;
    /** Format: int32 */
    transferInterval: number;
    sensorStations: AdminSensorStationsResponse;
  }

  export interface AccessPointConfigResponse {
    roomName: string;
    pairingMode: boolean;
    /** Format: int32 */
    transferInterval: number;
    sensorStations: SensorStationInfo[];
  }

  export interface Limits {
    /** Format: double */
    lowerLimit: number;
    /** Format: double */
    upperLimit: number;
  }

  export interface SensorInfo {
    sensorName: string;
    limits: Limits;
    /** Format: int32 */
    alarmThresholdTime: number;
  }

  export interface SensorStationInfo {
    bdAddress: string;
    sensors: SensorInfo[];
  }
}

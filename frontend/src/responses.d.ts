declare namespace Responses {
  declare interface GrantedAuthority {
    authority: string;
  }

  declare interface Person {
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

  declare interface SensorStationPersonReference {
    /** Format: uuid */
    id: string;
    inDashboard: boolean;
    assigned: boolean;
    deleted: boolean;
  }

  declare interface MessageResponse {
    message: string;
  }

  declare interface Sensor {
    /** Format: uuid */
    sensorId: string;
    type: string;
    unit: string;
  }

  declare interface SensorLimits {
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

  declare interface AccessPoint {
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
    connected: boolean;
    unlocked: boolean;
    password: string;
    deleted: boolean;
  }

  declare interface SensorData {
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

  declare interface SensorStation {
    /** Format: uuid */
    deviceId: string;
    bdAddress: string;
    name: string;
    /** Format: int32 */
    dipSwitchId: number;
    reported: boolean;
    sensorData: SensorData[];
    sensorLimits: SensorLimits[];
    sensorStationPersonReferences: SensorStationPersonReference[];
    sensorStationPictures: SensorStationPicture[];
    authorities: GrantedAuthority[];
    connected: boolean;
    username: string;
    unlocked: boolean;
    password: string;
    deleted: boolean;
  }

  declare interface SensorStationPicture {
    /** Format: uuid */
    pictureId: string;
    picturePath: string;
    /** Format: date-time */
    timeStamp: string;
  }

  declare interface AuthenticationException {
    cause: object;
    stackTrace: any[];
    message: string;
    suppressed: any[];
    localizedMessage: string;
  }

  declare interface TokenExpiredException {
    cause: object;
    stackTrace: any[];
    message: string;
    suppressed: any[];
    localizedMessage: string;
  }

  declare interface AccessDeniedException {
    cause: object;
    stackTrace: any[];
    message: string;
    suppressed: any[];
    localizedMessage: string;
  }

  declare interface CreatedUserResponse {
    /** Format: uuid */
    id: string;
    username: string;
    /** Format: uuid */
    token: string;
    permissions: GrantedAuthority[];
  }

  declare interface TokenResponse {
    /** Format: uuid */
    token: string;
  }

  declare interface LoginResponse {
    /** Format: uuid */
    token: string;
    /** Format: uuid */
    personId: string;
    permissions: GrantedAuthority[];
  }

  declare interface PermissionResponse {
    permissions: GrantedAuthority[];
  }

  declare interface SensorStationsInnerResponse {
    /** Format: uuid */
    sensorStationId: string;
    roomName: string;
    name: string;
    /** base64 */
    newestPicture: string;
  }

  declare interface SensorStationsResponse {
    sensorStations: SensorStationsInnerResponse[];
  }

  declare interface AlarmResponse {
    sensor: Sensor;
    alarm: string;
  }

  declare interface SensorLimitsResponse {
    /** Format: date-time */
    timeStamp: string;
    /** Format: float */
    upperLimit: number;
    /** Format: float */
    lowerLimit: number;
    /** Format: int32 */
    thresholdDuration: number;
    sensor: Sensor;
    gardener: Person;
    deleted: boolean;
  }

  declare interface SensorStationDetailResponse {
    sensorStation: SensorStationInnerResponse;
  }

  declare interface SensorStationInnerResponse {
    /** Format: uuid */
    sensorStationId: string;
    bdAddress: string;
    /** Format: int32 */
    dipSwitchId: number;
    roomName: string;
    name: string;
    /** Format: int32 */
    transferInterval: number;
    gardener: Person;
    alarms: AlarmResponse[];
    unlocked: boolean;
    accessPointUnlocked: boolean;
    connected: boolean;
    deleted: boolean;
    sensorLimits: SensorLimitsResponse[];
    sensorStationPersonReferences: SensorStationPersonReference[];
    sensorStationPictures: SensorStationPicture[];
  }

  declare interface InnerPlantPicture {
    /** Format: uuid */
    pictureId: string;
    /** Format: date-time */
    timeStamp: string;
  }

  declare interface PlantPicturesResponse {
    pictures: InnerPlantPicture[];
    roomName: string;
    plantName: string;
  }

  declare interface SensorStationPublicInfo {
    name: string;
    roomName: string;
  }

  declare interface InnerSensors {
    /** Format: uuid */
    sensorId: string;
    sensorType: string;
    sensorUnit: string;
    values: TimeStampedSensorData[];
    sensorLimits: SensorLimits[];
  }

  declare interface SensorStationDataResponse {
    data: InnerSensors[];
    /** Format: uuid */
    sensorStationId: string;
  }

  declare interface TimeStampedSensorData {
    /** Format: date-time */
    timeStamp: string;
    /** Format: double */
    value: number;
    alarm: string;
    aboveLimit: boolean;
    belowLimit: boolean;
  }

  declare interface GardenerDashBoardResponse {
    assignedSensorStations: SensorStationBaseResponse[];
    addedSensorStations: SensorStationBaseResponse[];
  }

  declare interface SensorStationBaseResponse {
    /** Format: uuid */
    sensorStationId: string;
    bdAddress: string;
    /** Format: int32 */
    dipSwitchId: number;
    roomName: string;
    name: string;
    /** Format: int32 */
    transferInterval: number;
    gardener: Person;
    alarms: AlarmResponse[];
    unlocked: boolean;
    accessPointUnlocked: boolean;
    connected: boolean;
    deleted: boolean;
  }

  declare interface UserDashBoardResponse {
    sensorStations: SensorStationBaseResponse[];
  }

  declare interface AdminDashBoardResponse {
    /** Format: int32 */
    numOfUsers: number;
    /** Format: int32 */
    numOfConnectedSensorStations: number;
    /** Format: int32 */
    numOfConnectedAccessPoints: number;
    sensorStations: SensorStationBaseResponse[];
  }

  declare interface ListResponse {
    items: any[];
  }

  declare interface AdminSensorStationsResponse {
    sensorStations: SensorStationBaseResponse[];
  }

  declare interface AccessPointListResponse {
    accessPoints: InnerAccessPoint[];
  }

  declare interface AccessPointListResponseSensorStation {
    /** Format: uuid */
    sensorStationId: string;
    bdAddress: string;
    /** Format: int32 */
    dipSwitchId: number;
    roomName: string;
    name: string;
    /** Format: int32 */
    transferInterval: number;
    gardener: Person;
    alarms: AlarmResponse[];
    unlocked: boolean;
    accessPointUnlocked: boolean;
    connected: boolean;
    deleted: boolean;
    reported: boolean;
  }

  declare interface InnerAccessPoint {
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
    sensorStations: AccessPointListResponseSensorStation[];
  }

  declare interface AccessPointConfigResponse {
    roomName: string;
    pairingMode: boolean;
    /** Format: int32 */
    transferInterval: number;
    sensorStations: SensorStationInfo[];
  }

  declare interface Limits {
    /** Format: double */
    lowerLimit: number;
    /** Format: double */
    upperLimit: number;
  }

  declare interface SensorInfo {
    sensorName: string;
    limits: Limits;
    /** Format: int32 */
    alarmThresholdTime: number;
  }

  declare interface SensorStationInfo {
    bdAddress: string;
    sensors: SensorInfo[];
  }
}

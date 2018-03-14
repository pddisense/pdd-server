//@flow
type Event = {
  addListener(callback: () => void): void,
  removeListener(callback: () => void): void,
  hasListener(callback: () => void): boolean,
  hasListeners(): boolean,
};

/**
 * Storage.
 */
type StorageChange = {
  newValue?: any,
  oldValue?: any
};

type StorageArea = {
  clear(callback?: () => void): void,
  get: (
    ((keys: string | Array<string> | Object, callback: (items: Object) => void) => void) &
    ((callback: (items: Object) => void) => void)
    ),
  getBytesInUse: (
    ((keys: string | Array<string> | null, callback: (bytesInUse: number) => void) => void) &
    ((callback: (bytesInUse: number) => void) => void)
    ),
  remove(keys: string | Array<string>, callback?: () => void): void,
  set(items: Object, callback?: () => void): void
};

type storage = {
  local: StorageArea,
  managed: StorageArea,
  sync: StorageArea,

  onChanged: Event & {
    addListener(callback: (changes: Object, areaName: 'local' | 'managed' | 'sync') => void): void
  }
};

/**
 * Alarms.
 */
type Alarm = {
  name: string,
  periodInMinutes?: number,
  scheduledTime: number
};

type AlarmInfo = {
  delayInMinutes?: number,
  periodInMinutes?: number,
  when?: number
};

type AlarmCallback = (alarm: Alarm) => void;
type WasClearedCallback = (wasCleared: boolean) => void;

type alarms = {
  clear(name?: string, callback?: WasClearedCallback): void,
  clearAll(callback?: WasClearedCallback): void,
  create: (
    ((name: string, alarmInfo: AlarmInfo) => void) &
    ((alarmInfo: AlarmInfo) => void)
    ),
  get: (
    ((name: string, callback: AlarmCallback) => void) &
    ((callback: AlarmCallback) => void)
    ),
  getAll(callback: (alarms: Array<Alarm>) => void): void,

  onAlarm: Event & {
    addListener(callback: AlarmCallback): void
  }
};

/**
 * Chrome.
 */
declare export var chrome: {
  alarms: alarms,
  storage: storage,
};

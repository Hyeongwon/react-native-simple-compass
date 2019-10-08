import { Dimensions, EmitterSubscription, NativeEventEmitter, NativeModules } from 'react-native';
const { RNSimpleCompass } = NativeModules;

let listener: EmitterSubscription | null = null;

export const SimpleCompass = {
  start(threshold: number = 0, callback: (heading: { degree: number; accuracy: number }) => void) {
    if (listener) {
      this.stop();
    }

    const compassEventEmitter = new NativeEventEmitter(RNSimpleCompass);
    listener = compassEventEmitter.addListener('HeadingUpdated', course => {
      const correctedCourse = { ...course };
      const { height, width } = Dimensions.get('window');
      if (width > height) {
        correctedCourse.degree += 90;
      }
      callback(correctedCourse);
    });

    RNSimpleCompass.start(threshold);
  },

  stop() {
    if (listener) {
      listener.remove();
      listener = null;
    }

    RNSimpleCompass.stop();
  },
};

export default SimpleCompass;

import {NativeEventEmitter, NativeModules} from 'react-native';

const {FoodDataModule} = NativeModules;
const FoodDataEvents = new NativeEventEmitter(FoodDataModule);

interface FoodDataInterface {
  getData(startTime: string): Promise<number>;
}

export default FoodDataModule as FoodDataInterface;
export {FoodDataEvents};

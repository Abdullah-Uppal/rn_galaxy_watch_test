import {NativeModules} from 'react-native';

const {FoodDataModule} = NativeModules;

interface FoodDataInterface {
  getData(startTime: string): Promise<number>;
}

export default FoodDataModule as FoodDataInterface;

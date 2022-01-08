import * as React from 'react';
import * as PropTypes from 'prop-types';
import {UnityModule} from './UnityModule';
import {UnityMessage, UnityMessageImpl} from './UnityMessage';
import {
  NativeModules,
  requireNativeComponent,
  ViewProps,
  View,
} from 'react-native';
import {
  UnityRequestHandler,
  UnityRequestHandlerImpl,
} from './UnityRequestHandler';

const {UIManager} = NativeModules;
const {ViewPropTypes} = require('react-native');

/** Prop definitions for a UnityView instance. */
export interface UnityViewProps extends ViewProps {
  /** Receive plain text message from unity. */
  onMessage?: (message: string) => void;
  /** Receive JSON message from unity. */
  onUnityMessage?: (message: UnityMessage) => void;
  /** Receive JSON request from unity. */
  onUnityRequest?: (handler: UnityRequestHandler) => void;
}

export default class UnityView extends React.Component<UnityViewProps> {
  private m_registrationToken: number;

  public static propTypes = {
    ...ViewPropTypes,
    onMessage: PropTypes.func,
  };

  public constructor(props: any) {
    super(props);

    this.m_registrationToken = UnityModule.addMessageListener(message => {
      if (message instanceof UnityMessageImpl) {
        if (this.props.onUnityMessage) {
          this.props.onUnityMessage(message);
        }
      } else if (message instanceof UnityRequestHandlerImpl) {
        if (this.props.onUnityRequest) {
          this.props.onUnityRequest(message);
        }
      } else if (typeof message === 'string') {
        if (this.props.onMessage) {
          this.props.onMessage(message);
        }
      }
    });
  }

  public componentDidMount() {
    console.log('TS: Unity Mounted');
  }
  public componentWillUnmount() {
    console.log('TS: Unity Unmounted');
    UnityModule.removeMessageListener(this.m_registrationToken);
    UnityModule.clear();
  }

  public render() {
    const {onUnityMessage, onMessage, ...props} = this.props;
    return (
      <View {...props}>
        <NativeUnityView
          style={{
            position: 'absolute',
            left: 0,
            right: 0,
            top: 0,
            bottom: 0,
          }}></NativeUnityView>
        {this.props.children}
      </View>
    );
  }
}

const NativeUnityView = requireNativeComponent<UnityViewProps>('RNUnityView');

import { NativeModules } from 'react-native';
import { UnityMessage, UnityMessagePrefix, UnityMessageImpl, UnityMessageType } from './UnityMessage';
import { IUnityRequest } from './UnityRequest';
const { UnityNativeModule } = NativeModules;

type OnCancelCallback<TRequest extends IUnityRequest = IUnityRequest> = (handler: UnityRequestHandler<TRequest>) => void;

export interface UnityRequestHandler<TRequest extends IUnityRequest = IUnityRequest> {
    readonly isCanceled: boolean;
    readonly message: TRequest;
    sendResponse(data: TRequest ["_"]): void;
    sendError(error: any): void;
    onCancel(callback: OnCancelCallback<TRequest>);
}

export class UnityRequestHandlerImpl<TRequest extends IUnityRequest = IUnityRequest> implements UnityRequestHandler<TRequest> {
    private m_responseSent: boolean;
    private m_isCanceled: boolean;
    private m_onCancel: OnCancelCallback<TRequest>[];
    private m_onClose: (uuid: number) => void;

    constructor(message: UnityMessageImpl, onClose: (uuid: number) => void) {
        if (!message.isRequest) {
            throw new Error(`Cannot create instance of UnityRequestHandler. Provided message is not a request type.`)
        }

        this.message = message as any;
        this.m_onCancel = [];
        this.m_onClose = onClose;
    }

    public get isCanceled(): boolean {
        return this.m_isCanceled;
    }

    public message: TRequest;

    public sendResponse(data?: TRequest ["_"]): void {
        this.m_responseSent = true;
        UnityNativeModule.postMessage(
            'UnityMessageManager', 'onRNMessage', UnityMessagePrefix + JSON.stringify({
                id: this.message.id,
                type: UnityMessageType.Response,
                uuid: this.message.uuid,
                data: data,
            })
        );

        this.close();
    }

    public sendError(error: any): void {
        this.m_responseSent = true;
        UnityNativeModule.postMessage('UnityMessageManager', 'onRNMessage', UnityMessagePrefix + JSON.stringify({
            id: this.message.id,
            type: UnityMessageType.Error,
            uuid: this.message.uuid,
            data: error
        }));

        this.close();
    }

    public close(): void {
        if (this.message.uuid) {
            if (!this.m_responseSent) {
                this.m_responseSent = true;
                if (this.m_isCanceled) {
                    this.sendCanceled();
                } else {
                    this.sendResponse();
                }
            }

            this.m_onClose(this.message.uuid);
        }
    }

    public onCancel(callback: OnCancelCallback<TRequest>) {
        if (typeof callback !== "function") {
            throw new Error("Cancellation callback is not a function!");
        }

        this.m_onCancel.push(callback);
    }

    public cancel(): void {
        this.m_isCanceled = true;

        while (this.m_onCancel.length > 0) {
            this.m_onCancel.shift()(this);
        }
        
        this.close();
    }

    sendCanceled(): void {
        this.m_responseSent = true;
        UnityNativeModule.postMessage('UnityMessageManager', 'onRNMessage', UnityMessagePrefix + JSON.stringify({
            id: this.message.id,
            type: UnityMessageType.Canceled,
            uuid: this.message.uuid
        }));

        this.close();
    }
}

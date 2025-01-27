﻿using System;
using System.Runtime.CompilerServices;
using System.Threading;
using UniRx;
using UnityEngine;

namespace ReactNative
{
    public sealed partial class UnityMessageManager
    {
        private sealed class UnityMessageHandlerImpl : IUnityMessageHandler, IDisposable
        {
            private readonly string raw;
            private IDisposable deferral;
            private CancellationTokenSource cts;

            public UnityMessageHandlerImpl(UnityMessage message, string raw)
            {
                this.raw = raw;
                this.Message = message;
            }

            public UnityMessage Message { get; }

            public bool IsRequest => this.Message.IsRequest;

            public bool IsDeferred { get; private set; }

            public bool ResponseSent { get; private set; }

            public CancellationToken CancellationToken => this.CancellationTokenSource.Token;

            public CancellationTokenSource CancellationTokenSource => (this.cts ?? (this.cts = new CancellationTokenSource()));

            public IDisposable GetDeferral()
            {
                this.IsDeferred = true;
                return this.deferral ?? (this.deferral = Disposable.Create(() =>
                {
                    this.deferral = null;
                    this.Dispose();
                }));
            }

            public void SendResponse(object data)
            {
                if (this.IsRequest)
                {
                    this.ResponseSent = true;
                    UnityMessageManager.SendResponse(
                        this.Message.id,
                        this.Message.uuid.Value,
                        data);
                }
                else
                {
                    Debug.unityLogger.LogError("messaging", "This message is not a request type.");
                }
            }

            public void SendCanceled()
            {
                if (this.IsRequest)
                {
                    this.ResponseSent = true;
                    UnityMessageManager.SendCanceled(
                        this.Message.id,
                        this.Message.uuid.Value);
                }
                else
                {
                    Debug.unityLogger.LogError("messaging", "This message is not a request type.");
                }
            }

            public void SendError(UnityRequestException error)
            {
                if (this.IsRequest)
                {
                    this.ResponseSent = true;

                    if (string.IsNullOrEmpty(error.rawInput))
                    {
                        error.rawInput = this.raw;
                    }

                    Debug.LogError(error);

                    UnityMessageManager.SendError(
                        this.Message.id,
                        this.Message.uuid.Value,
                        error);
                }
                else
                {
                    Debug.unityLogger.LogError("messaging", "This message is not a request type.");
                }
            }

            public void SendError(
                Exception error,
                [CallerMemberName] string memberName = "",
                [CallerFilePath] string sourceFilePath = "",
                [CallerLineNumber] int sourceLineNumber = 0)
            {
                this.SendError(
                    new UnityRequestException(
                        error,
                        raw,
                        memberName,
                        sourceFilePath,
                        sourceLineNumber));
            }

            public void Dispose()
            {
                if (this.IsRequest)
                {
                    instance?.RemoveIncommingRequest(this.Message.uuid.Value);

                    if (!this.ResponseSent)
                    {
                        Debug.unityLogger.LogWarning("messaging", $"Missing request response: id={this.Message.id} type={this.Message.type} uuid={this.Message.uuid.Value}");

                        this.ResponseSent = true;
                        UnityMessageManager.SendResponse(
                            this.Message.id,
                            this.Message.uuid.Value,
                            null);
                    }
                }
            }

            internal void NotifyCancelled()
                => this.CancellationTokenSource.Cancel();
        }
    }
}

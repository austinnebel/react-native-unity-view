using System;
using System.Runtime.CompilerServices;
using UnityEngine.Scripting;

namespace ReactNative
{
    [Preserve]
    public sealed class UnityRequestException : Exception
    {
        internal UnityRequestException(string message,
                                       string rawInput,
                                       [CallerMemberName] string memberName = "",
                                       [CallerFilePath] string sourceFilePath = "",
                                       [CallerLineNumber] int sourceLineNumber = 0)
            : this(message, rawInput, null, default(UnityMessage), memberName, sourceFilePath, sourceLineNumber)
        { }

        public UnityRequestException(string message,
                                     [CallerMemberName] string memberName = "",
                                     [CallerFilePath] string sourceFilePath = "",
                                     [CallerLineNumber] int sourceLineNumber = 0)
            : this(message, null, null, default(UnityMessage), memberName, sourceFilePath, sourceLineNumber)
        { }

        internal UnityRequestException(string message,
                                       string rawInput,
                                       Exception innerException,
                                       [CallerMemberName] string memberName = "",
                                       [CallerFilePath] string sourceFilePath = "",
                                       [CallerLineNumber] int sourceLineNumber = 0)
            : this(message, rawInput, innerException, default(UnityMessage), memberName, sourceFilePath, sourceLineNumber)
        { }

        public UnityRequestException(string message,
                                     Exception innerException,
                                     [CallerMemberName] string memberName = "",
                                     [CallerFilePath] string sourceFilePath = "",
                                     [CallerLineNumber] int sourceLineNumber = 0)
            : this(message, null, innerException, default(UnityMessage), memberName, sourceFilePath, sourceLineNumber)
        { }

        internal UnityRequestException(Exception innerException,
                                       string rawInput,
                                       [CallerMemberName] string memberName = "",
                                       [CallerFilePath] string sourceFilePath = "",
                                       [CallerLineNumber] int sourceLineNumber = 0)
            : this(null, rawInput, innerException, default(UnityMessage), memberName, sourceFilePath, sourceLineNumber)
        { }

        public UnityRequestException(Exception innerException,
                                     [CallerMemberName] string memberName = "",
                                     [CallerFilePath] string sourceFilePath = "",
                                     [CallerLineNumber] int sourceLineNumber = 0)
            : this(null, null, innerException, default(UnityMessage), memberName, sourceFilePath, sourceLineNumber)
        { }

        internal UnityRequestException(UnityMessage errorMessage,
                                       string rawInput,
                                       [CallerMemberName] string memberName = "",
                                       [CallerFilePath] string sourceFilePath = "",
                                       [CallerLineNumber] int sourceLineNumber = 0)
            : this(null, rawInput, null, errorMessage, memberName, sourceFilePath, sourceLineNumber)
        { }

        public UnityRequestException(UnityMessage errorMessage,
                                     [CallerMemberName] string memberName = "",
                                     [CallerFilePath] string sourceFilePath = "",
                                     [CallerLineNumber] int sourceLineNumber = 0)
            : this(null, null, null, errorMessage, memberName, sourceFilePath, sourceLineNumber)
        { }

        private UnityRequestException(string message,
                                      string rawInput,
                                      Exception innerException,
                                      UnityMessage errorMessage,
                                      string memberName = "",
                                      string sourceFilePath = "",
                                      int sourceLineNumber = 0)
            : base(message, innerException)
        {
            this.rawInput = rawInput;
            this.error = errorMessage;
            this.memberName = memberName;
            this.sourceFilePath = sourceFilePath;
            this.sourceLineNumber = sourceLineNumber;
        }

        [Preserve]
        internal string rawInput { get; set; }

        [Preserve]
        public UnityMessage error { get; }

        [Preserve]
        public string memberName { get; }

        [Preserve]
        public string sourceFilePath { get; }

        [Preserve]
        public int sourceLineNumber { get; }

        [Preserve]
        public override string ToString()
            => $@"{(this.InnerException ?? this).GetType().Name}: {this.InnerException?.Message ?? this.Message}

{this.memberName ?? "<unknown>"} at {this.sourceFilePath ?? "unknown_file"}:{this.sourceLineNumber}

{this.rawInput}

{base.ToString()}";
    }
}

package com.fanwe.lib.http;

import com.fanwe.lib.http.callback.IUploadProgressCallback;
import com.fanwe.lib.http.callback.RequestCallback;
import com.fanwe.lib.http.utils.HttpLogger;
import com.fanwe.lib.http.utils.TransmitParam;
import com.fanwe.lib.task.FTask;


class RequestTask extends FTask implements IUploadProgressCallback
{
    private final IRequest mRequest;
    private final RequestCallback mCallback;

    public RequestTask(IRequest request, RequestCallback callback)
    {
        mRequest = request;
        mCallback = callback;

        mRequest.setUploadProgressCallback(this);
    }

    private String getLogPrefix()
    {
        return String.valueOf(this);
    }

    @Override
    protected void onRun() throws Exception
    {
        HttpLogger.e(getLogPrefix() + " 1 onRun---------->");

        synchronized (RequestTask.this)
        {
            runOnUiThread(mStartRunnable);
            HttpLogger.i(getLogPrefix() + " 2 waitThread");
            RequestTask.this.wait(); //等待开始回调完成
        }

        HttpLogger.i(getLogPrefix() + " 4 resumeThread");

        final IResponse response = mRequest.execute();
        mCallback.setResponse(response);
        mCallback.onSuccessBackground();

        HttpLogger.i(getLogPrefix() + " 5 onSuccess");

        runOnUiThread(mSuccessRunnable);
    }

    private Runnable mStartRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (RequestTask.this)
            {
                mCallback.onStart();
                HttpLogger.i(getLogPrefix() + " 3 notifyThread");
                RequestTask.this.notifyAll();
            }
        }
    };

    private Runnable mSuccessRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mCallback.onSuccessBefore();
            mCallback.onSuccess();
        }
    };

    @Override
    protected void onError(final Exception e)
    {
        super.onError(e);
        HttpLogger.i(getLogPrefix() + " onError:" + e);
        if (isCancelled())
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mCallback.onCancel();
                }
            });
        } else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mCallback.onError(e);
                }
            });
        }
    }

    @Override
    protected void onFinally()
    {
        super.onFinally();
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mCallback.onFinish();
            }
        });
    }

    @Override
    public void onProgressUpload(TransmitParam param)
    {
        mCallback.onProgressUpload(param);
    }
}

import express from "express";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import crypto from "node:crypto";

const app = express();
app.disable("x-powered-by");
app.use(helmet());
app.use(express.json({limit:"32kb"}));
app.use(rateLimit({windowMs:60_000, limit:30, standardHeaders:true, legacyHeaders:false}));

const API_KEY = process.env.OPENAI_API_KEY;
const MODEL = process.env.OPENAI_MODEL || "gpt-4o-mini";
const APP_TOKEN = process.env.CALLCATCHER_APP_TOKEN;
if (!API_KEY || !APP_TOKEN) {
  console.error("Set OPENAI_API_KEY and CALLCATCHER_APP_TOKEN before starting.");
  process.exit(1);
}

// Short-lived process memory, keyed by an app-issued user ID.
// For production multi-instance deployment use encrypted persistent storage with TTL.
const memory = new Map();
const TTL = 24 * 60 * 60 * 1000;
app.use("/v1", (req,res,next) => {
  const token = req.get("authorization")?.replace(/^Bearer\s+/i,"");
  if (!token || !crypto.timingSafeEqual(
    Buffer.from(token.padEnd(APP_TOKEN.length).slice(0,APP_TOKEN.length)),
    Buffer.from(APP_TOKEN)
  ) || token.length !== APP_TOKEN.length) return res.status(401).json({error:"Unauthorized"});
  next();
});

app.post("/v1/chat", async (req,res) => {
  try {
    const {messages, conversationId} = req.body ?? {};
    if (!Array.isArray(messages) || messages.length < 1 || messages.length > 30 ||
        messages.some(m => !["user","assistant"].includes(m?.role) ||
          typeof m?.content !== "string" || m.content.length > 6000)) {
      return res.status(400).json({error:"Invalid messages"});
    }
    const id = typeof conversationId === "string" && /^[\w-]{1,80}$/.test(conversationId)
      ? conversationId : "default";
    const now = Date.now();
    const prior = memory.get(id);
    const history = prior && now-prior.updated < TTL ? prior.messages : [];
    const merged = [...history, ...messages].slice(-24);
    const upstream = await fetch("https://api.openai.com/v1/chat/completions", {
      method:"POST",
      headers:{"Authorization":`Bearer ${API_KEY}`,"Content-Type":"application/json"},
      body:JSON.stringify({
        model:MODEL,
        messages:[
          {role:"system",content:"You are CallCatcher's helpful assistant. Provide safe, concise caller-response suggestions. Do not claim to have answered, recorded, or controlled a call. For actions affecting calls, require explicit user confirmation and only describe actions supported by the app."},
          ...merged
        ],
        temperature:0.4
      }),
      signal:AbortSignal.timeout(30_000)
    });
    const data = await upstream.json();
    if (!upstream.ok) return res.status(502).json({error:"AI provider request failed"});
    const reply = data.choices?.[0]?.message?.content?.trim();
    if (!reply) return res.status(502).json({error:"Empty model response"});
    memory.set(id,{messages:[...merged,{role:"assistant",content:reply}].slice(-24),updated:now});
    res.json({reply});
  } catch (e) {
    res.status(502).json({error:"AI service unavailable"});
  }
});
app.get("/health",(_req,res)=>res.json({status:"ok"}));
const port=Number(process.env.PORT||8080);
app.listen(port,()=>console.log(`CallCatcher gateway listening on ${port}`));

// Programme facts are deterministic records; AI does not rewrite source requirements.
export function sourceFacts(data,id,section){
 if(!['universities','requirements'].includes(section))return null;
 const codes=new Set(data.byCourse[id]||[]),rows=data.records.filter(r=>codes.has(r.code));
 if(!rows.length)return null;
 return ['KUCCPS verified source: 2025/2026 Degree Programmes','Listed programme variants in this course family. Availability is verified for this edition, not later intakes.',...rows.map(r=>`${r.institution} — ${r.programme} | Programme code ${r.code} | PDF page ${r.page}${section==='requirements'?`\nPrinted subject requirements: ${r.subjectRequirements.join('; ')||'Not specified in this row. Confirm with KUCCPS or the institution.'}`:''}`),'Historical 2023/2022 cutoffs are not current entry requirements. Suitability scores and AI career guidance are not verified by KUCCPS.'].join('\n\n');
}
let catalogue;
export async function verifiedCourseFacts(id,section){
 catalogue??=fetch('./kuccps_2025.json').then(r=>{if(!r.ok)throw Error('Catalogue unavailable');return r.json();}).catch(e=>{catalogue=null;throw e;});
 return sourceFacts(await catalogue,id,section);
}

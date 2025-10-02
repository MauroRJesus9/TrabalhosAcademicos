using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using TPV2.Data;
using TPV2.Models;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;

namespace TPV2.Controllers
{

    [Authorize]
    public class ScoreClientsController : Controller
    {
        private readonly ApplicationDbContext _context;

        public ScoreClientsController(ApplicationDbContext context)
        {
            _context = context;
        }

        [AllowAnonymous]
        // GET: ScoreClients
        public async Task<IActionResult> Index(int? id, string? userId)
        {
            var user = User.FindFirstValue(ClaimTypes.NameIdentifier);

            if (User.IsInRole("Gestor"))
            {
                var applicationDbContext = _context.ScoreClient.Include(s => s.Reserves)
                                                            .Include(s => s.SCClientID)
                                                            .Include(s => s.SCFuncionarioID)
                                                            .Include(s => s.SCProprietarioID)
                                                            .Where(s => s.ProprietarioId.Contains(user));
                return View(await applicationDbContext.ToListAsync());
            }

            if (userId != null)
            {
                var applicationDbContextId = _context.ScoreClient.Include(s => s.Reserves)
                                                            .Include(s => s.SCClientID)
                                                            .Include(s => s.SCFuncionarioID)
                                                            .Include(s => s.SCProprietarioID)
                                                            .Where(s => s.ClientId.Contains(userId));
                return View(await applicationDbContextId.ToListAsync());
            }


            if (id != null)
            {

                 var applicationDbContextId = _context.ScoreClient.Include(s => s.Reserves)
                                                            .Include(s => s.SCClientID)
                                                            .Include(s => s.SCFuncionarioID)
                                                            .Include(s => s.SCProprietarioID)
                                                            .Where(s => s.PropertyId == id);
                return View(await applicationDbContextId.ToListAsync());
            }
            else
            {
                var applicationDbContext = _context.ScoreClient.Include(s => s.Reserves)
                                                            .Include(s => s.SCClientID)
                                                            .Include(s => s.SCFuncionarioID)
                                                            .Include(s => s.SCProprietarioID);
                return View(await applicationDbContext.ToListAsync());
            }
            
        }

        // GET: ScoreClients/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var scoreClient = await _context.ScoreClient
                .Include(s => s.Reserves)
                .Include(s => s.SCClientID)
                .Include(s => s.SCFuncionarioID)
                .Include(s => s.SCProprietarioID)
                .FirstOrDefaultAsync(m => m.ScoreClientId == id);
            if (scoreClient == null)
            {
                return NotFound();
            }

            return View(scoreClient);
        }
        [Authorize(Roles = "Cliente")]
        // GET: ScoreClients/Create
        public IActionResult Create(int? reserveid, string? clientid, int? propertyid)
        {
            //Verificar se já existe alguma avaliação com este id de reserva
            var pesquisa_id = (from reserva in _context.ScoreClient
                              where reserva.ReserveId == reserveid
                              select reserva);

            if(pesquisa_id.Any()) { //Já existe uma avaliação com o mesmo id de reserva

                return Redirect("/Reserves");
            }
            else //Adiciona avaliação
            {
                if (reserveid != null && clientid != null && propertyid != null)
                {
                    ViewData["ReserveId"] = new SelectList(_context.Reserves.Where(r => r.ReserveId == reserveid), "ReserveId", "ReserveId");
                    ViewData["ClientId"] = new SelectList(_context.AplicationUser.Where(c => c.Id == clientid), "Id", "UserName");
                    ViewData["PropertyId"] = new SelectList(_context.Properties.Where(p => p.PropertyId == propertyid), "PropertyId", "Name");
                }
                else
                {
                    ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "ReserveId");
                    ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName");
                    ViewData["PropertyId"] = new SelectList(_context.Properties, "PropertyId", "Name");
                }
                var users = (from user in _context.Users
                             join userRole in _context.UserRoles
                             on user.Id equals userRole.UserId
                             join role in _context.Roles
                             on userRole.RoleId equals role.Id
                             where role.Name == "Funcionario"
                             select user);

                ViewData["FuncionarioId"] = new SelectList(users, "Id", "UserName");

                

                var userProprietario = (from user in _context.AplicationUser
                                        join p in _context.Properties on user.Id equals p.ProprietarioId
                                        where p.PropertyId == propertyid
                                        select user);


                ViewData["ProprietarioId"] = new SelectList(userProprietario, "Id", "UserName");
                return View();
            }
        }

        // POST: ScoreClients/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("ScoreClientId,ReserveId,Comments,ScorePropriedade,ScoreProprietario,ProprietarioId,ScoreFuncionário,FuncionarioId, PropertyId")] ScoreClient scoreClient)
        {
            scoreClient.isRemoved = false;
            scoreClient.ClientId = User.FindFirstValue(ClaimTypes.NameIdentifier);


            if (ModelState.IsValid)
            {
                _context.Add(scoreClient);
                await _context.SaveChangesAsync();
                if (User.IsInRole("Cliente"))
                {
                    return RedirectToAction("Index", new { userId = User.FindFirstValue(ClaimTypes.NameIdentifier) });
                }
                else
                {
                    return RedirectToAction(nameof(Index));
                }
            }
            ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "Observations", scoreClient.ReserveId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreClient.ClientId);
            ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreClient.FuncionarioId);
            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreClient.ProprietarioId);
            return View(scoreClient);
        }

        // GET: ScoreClients/Edit/5
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var scoreClient = await _context.ScoreClient.FindAsync(id);
            if (scoreClient == null)
            {
                return NotFound();
            }
            ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "ReserveId", scoreClient.ReserveId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "Id", scoreClient.ClientId);
            ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser, "Id", "Id", scoreClient.FuncionarioId);
            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "Id", scoreClient.ProprietarioId);
            return View(scoreClient);
        }

        // POST: ScoreClients/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [Authorize(Roles = "Admin")]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("ScoreClientId,ClientId,ReserveId,Comments,ScorePropriedade,ScoreProprietario,ProprietarioId,ScoreFuncionário,FuncionarioId,isRemoved")] ScoreClient scoreClient)
        {
            if (id != scoreClient.ScoreClientId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(scoreClient);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!ScoreClientExists(scoreClient.ScoreClientId))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "Observations", scoreClient.ReserveId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreClient.ClientId);
            ViewData["FuncionarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreClient.FuncionarioId);
            ViewData["ProprietarioId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreClient.ProprietarioId);
            return View(scoreClient);
        }

        // GET: ScoreClients/Delete/5
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var scoreClient = await _context.ScoreClient
                .Include(s => s.Reserves)
                .Include(s => s.SCClientID)
                .Include(s => s.SCFuncionarioID)
                .Include(s => s.SCProprietarioID)
                .FirstOrDefaultAsync(m => m.ScoreClientId == id);
            if (scoreClient == null)
            {
                return NotFound();
            }

            return View(scoreClient);
        }

        // POST: ScoreClients/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var scoreClient = await _context.ScoreClient.FindAsync(id);
            _context.ScoreClient.Remove(scoreClient);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool ScoreClientExists(int id)
        {
            return _context.ScoreClient.Any(e => e.ScoreClientId == id);
        }
    }
}

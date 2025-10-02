using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc.Rendering;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Roles
{
    static public class RoleUtils
    {
        private static List<IdentityRole> roles = new List<IdentityRole>
        {
            new IdentityRole{  Id = "8f282582-8f2b-4259-8c2f-4b169c25a18d", Name = "Admin", NormalizedName = "ADMIN", ConcurrencyStamp = "1"},
            new IdentityRole{  Id = "e2ab7093-7693-411d-a0a0-a2c3d9a0fe2a", Name = "Gestor", NormalizedName = "GESTOR", ConcurrencyStamp = "2"},
            new IdentityRole{  Id = "45ec2e27-0c72-473f-ae89-c3b7ca78547f", Name = "Funcionario", NormalizedName = "FUNCIONARIO", ConcurrencyStamp = "3"},
            new IdentityRole{  Id = "f30b26e7-2709-4784-a411-47e7038312c1", Name = "Cliente", NormalizedName = "CLIENTE", ConcurrencyStamp = "4"},
        };

        static public List<IdentityRole> All
        {
            get
            {
                return roles;
            }
        }

        static public IEnumerable<SelectListItem> RegistSelectList
        {
            get
            {
                return roles.Where(r => r.Name.Contains("Gestor") || r.Name.Contains("Cliente"))
                    .Select(r => new SelectListItem()
                    {
                        Text = r.Name,
                        Value = r.Id.ToString()
                    });
            }
        }
    }
}
